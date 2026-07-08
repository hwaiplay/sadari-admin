package org.sadari.admin.sadariadmin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sadari.admin.sadariadmin.admin.service.AdminRedisAuthService;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Redis 토큰을 Spring Security 인증 정보로 변환하는 필터.
 */
@Component
public class RedisAuthenticationFilter extends OncePerRequestFilter {

    /** Redis 관리자 인증 토큰 서비스. */
    private final AdminRedisAuthService adminRedisAuthService;

    public RedisAuthenticationFilter(AdminRedisAuthService adminRedisAuthService) {
        this.adminRedisAuthService = adminRedisAuthService;
    }

    /**
     * 로그인과 공통코드 API는 Redis 인증 필터를 타지 않는다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "/api/auth/login".equals(uri) || uri.startsWith("/api/codes/");
    }

    /**
     * 요청 쿠키의 Redis 토큰이 유효하면 SecurityContext에 관리자 권한을 넣는다.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        AdminSessionVO admin = adminRedisAuthService.getAdminSessionDtl(request);
        if (admin != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + admin.getAuthCode())
            );
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(admin, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}

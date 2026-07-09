package org.sadari.admin.sadariadmin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sadari.admin.sadariadmin.admin.service.AdminRedisAuthService;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.AuthConstant;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Redis 토큰 기반 Spring Security 인증 필터
 */
@Component
public class RedisAuthenticationFilter extends OncePerRequestFilter {

    /** Redis 관리자 인증 토큰 서비스 */
    private final AdminRedisAuthService adminRedisAuthService;

    /**
     * Redis 인증 필터 생성
     * @Author SeungHyeon.Kang
     * @param adminRedisAuthService
     * @return
     */
    public RedisAuthenticationFilter(AdminRedisAuthService adminRedisAuthService) {
        this.adminRedisAuthService = adminRedisAuthService;
    }

    /**
     * Redis 인증 필터 제외 여부 확인
     * @Author SeungHyeon.Kang
     * @param request
     * @return
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 로그인 API와 공통코드 조회 API는 인증 없이 접근 가능하므로 필터 제외로 분기한다
        return Constant.API_AUTH_LOGIN.equals(uri) || uri.startsWith(Constant.API_CODES_PREFIX);
    }

    /**
     * Redis 인증 처리
     * @Author SeungHyeon.Kang
     * @param request
     * @param response
     * @param filterChain
     * @return
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        AdminSessionVO admin = adminRedisAuthService.getAdminSessionDtl(request);
        // Redis에서 관리자 세션을 조회했고 현재 SecurityContext가 비어 있을 때 인증 객체를 생성한다
        if (!StringUtil.isEmpty(admin) && StringUtil.isEmpty(SecurityContextHolder.getContext().getAuthentication())) {
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority(AuthConstant.ROLE_PREFIX + admin.getAuthCode())
            );
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(admin, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}

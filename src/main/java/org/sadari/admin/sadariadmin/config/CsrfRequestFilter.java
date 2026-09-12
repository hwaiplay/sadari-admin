package org.sadari.admin.sadariadmin.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 관리자 쿠키 인증 상태 변경 요청의 CSRF를 차단한다. */
@Component
public class CsrfRequestFilter extends OncePerRequestFilter {

    /** 브라우저의 교차 출처 단순 요청으로 보낼 수 없는 검증 헤더 */
    public static final String CSRF_HEADER_NAME = "X-SADARI-CSRF";

    /** 관리자 프런트가 보내는 검증 헤더 값 */
    public static final String CSRF_HEADER_VALUE = "1";

    /** 서버 상태를 변경하지 않는 HTTP 메서드 */
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    /** 상태 변경 요청에 검증 헤더가 없으면 요청을 거절한다. */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response
                                   , FilterChain filterChain) throws ServletException, IOException {
        if (SAFE_METHODS.contains(request.getMethod())
                || CSRF_HEADER_VALUE.equals(request.getHeader(CSRF_HEADER_NAME))) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":1004,\"message\":\"접근 권한이 없습니다.\",\"data\":null}");
    }
}

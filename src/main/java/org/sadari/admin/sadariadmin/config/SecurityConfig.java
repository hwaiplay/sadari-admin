package org.sadari.admin.sadariadmin.config;

import jakarta.servlet.http.HttpServletResponse;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 관리자 API 보안 설정.
 */
@Configuration
public class SecurityConfig {

    /** Redis 인증 필터. */
    private final RedisAuthenticationFilter redisAuthenticationFilter;

    /** 메뉴 권한 필터. */
    private final MenuAuthorizationFilter menuAuthorizationFilter;

    public SecurityConfig(
            RedisAuthenticationFilter redisAuthenticationFilter,
            MenuAuthorizationFilter menuAuthorizationFilter
    ) {
        this.redisAuthenticationFilter = redisAuthenticationFilter;
        this.menuAuthorizationFilter = menuAuthorizationFilter;
    }

    /**
     * Redis 토큰 기반 인증과 메뉴별 권한을 설정한다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/codes/**").permitAll()
                        .requestMatchers("/api/auth/logout", "/api/auth/me").authenticated()
                        .requestMatchers("/api/menu-permissions/**").authenticated()
                        .requestMatchers("/api/menus/**", "/api/code-manage/**").authenticated()
                        .requestMatchers("/api/employees/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeResult(response, HttpServletResponse.SC_UNAUTHORIZED, ResultData.fail(ResultEnum.AUTH_FAIL)))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeResult(response, HttpServletResponse.SC_FORBIDDEN, ResultData.fail(ResultEnum.FORBIDDEN)))
                )
                .addFilterBefore(redisAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(menuAuthorizationFilter, RedisAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Spring Security 예외 응답을 공통 응답으로 작성한다.
     */
    private void writeResult(HttpServletResponse response, int status, ResultData resultData) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format(
                "{\"code\":%d,\"message\":\"%s\",\"data\":null}",
                resultData.getCode(),
                resultData.getMessage()
        ));
    }
}

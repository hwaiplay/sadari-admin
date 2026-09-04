package org.sadari.admin.sadariadmin.config;

import jakarta.servlet.http.HttpServletResponse;
import org.sadari.admin.sadariadmin.common.constant.Constant;
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
 * fileName       : SecurityConfig
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 관리자 API 보안 설정 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    현재 사용자 관리 API 인증 적용
 * 2026-08-07        SeungHyeon.Kang    업로드 이미지 조회에 관리자 인증 적용
 * 2026-08-12        SeungHyeon.Kang    알림 아이콘 관리 API 인증 적용
 * 2026-09-04        HanWon.Jang        독서 모임 관리 API 인증 적용
 */
@Configuration
public class SecurityConfig {

    /** Redis 인증 필터 */
    private final RedisAuthenticationFilter redisAuthenticationFilter;

    /**
     * 관리자 API 보안 설정 생성
     * @author SeungHyeon.Kang
     * @param redisAuthenticationFilter
     * @return
     */
    public SecurityConfig(
            RedisAuthenticationFilter redisAuthenticationFilter
    ) {
        this.redisAuthenticationFilter = redisAuthenticationFilter;
    }

    /**
     * Spring Security 필터 체인 생성
     * @author SeungHyeon.Kang
     * @param http
     * @return
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(Constant.API_AUTH_LOGIN).permitAll()
                        .requestMatchers(Constant.API_CODES_PATTERN).permitAll()
                        .requestMatchers(Constant.API_AUTH_LOGOUT, Constant.API_AUTH_ME).authenticated()
                        .requestMatchers(Constant.API_MENU_PERMISSION).authenticated()
                        .requestMatchers(Constant.API_MENUS_PATTERN, Constant.API_USER_MENUS_PATTERN
                                       , Constant.API_CODE_MANAGE_PATTERN, Constant.API_ALIM_TEMP_PATTERN
                                       , Constant.API_ALIM_ICON_PATTERN
                                       , Constant.API_POPUP_CONTENT_PATTERN, Constant.API_AUTH_GROUP_PATTERN
                                       , Constant.API_ADMIN_AUTHS_PATTERN, Constant.API_CURRENT_USERS_PATTERN
                                       , Constant.API_COMPLAINTS_PATTERN, Constant.API_READING_CLUBS_PATTERN
                                       , Constant.API_UPLOADS_PATTERN
                                       , Constant.API_WELCOME_PAGE_PATTERN).authenticated()
                        .requestMatchers(Constant.API_EMPLOYEES_PATTERN).authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeResult(response, HttpServletResponse.SC_UNAUTHORIZED, ResultData.fail(ResultEnum.AUTH_FAIL)))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeResult(response, HttpServletResponse.SC_FORBIDDEN, ResultData.fail(ResultEnum.FORBIDDEN)))
                )
                .addFilterBefore(redisAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Spring Security 예외 응답 작성
     * @author SeungHyeon.Kang
     * @param response
     * @param status
     * @param resultData
     * @return
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

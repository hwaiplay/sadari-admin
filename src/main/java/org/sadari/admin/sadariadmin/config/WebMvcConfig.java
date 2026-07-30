package org.sadari.admin.sadariadmin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * fileName       : WebMvcConfig
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 관리자 웹 MVC 설정
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    스케줄러 로그 조회 권한 경로 추가
 * 2026-07-30        SeungHyeon.Kang    팝업 콘텐츠 관리 권한 경로 추가
 * 2026-07-30        SeungHyeon.Kang    현재 사용자 조회 권한 경로 추가
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 메뉴 권한 인터셉터 */
    private final MenuPermissionInterceptor menuPermissionInterceptor;

    /** 관리자 웹 MVC 설정 생성 */
    public WebMvcConfig(MenuPermissionInterceptor menuPermissionInterceptor) {
        this.menuPermissionInterceptor = menuPermissionInterceptor;
    }

    /** 메뉴 권한 인터셉터 등록 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(menuPermissionInterceptor)
                .addPathPatterns(
                        "/api/menus/**",
                        "/api/user-menus/**",
                        "/api/code-manage/**",
                        "/api/alim-temps/**",
                        "/api/popup-contents/**",
                        "/api/auth-groups/**",
                        "/api/admin-auths/**",
                        "/api/schedule-logs/**",
                        "/api/current-users/**"
                );
    }
}

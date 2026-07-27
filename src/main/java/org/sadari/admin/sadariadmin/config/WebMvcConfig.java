package org.sadari.admin.sadariadmin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 관리자 웹 MVC 설정 */
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
                        "/api/auth-groups/**"
                );
    }
}

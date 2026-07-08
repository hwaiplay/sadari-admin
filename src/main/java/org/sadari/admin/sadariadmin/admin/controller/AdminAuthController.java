package org.sadari.admin.sadariadmin.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sadari.admin.sadariadmin.admin.service.AdminAuthService;
import org.sadari.admin.sadariadmin.admin.service.AdminRedisAuthService;
import org.sadari.admin.sadariadmin.admin.vo.AdminLoginRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.config.AuthRedisProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 인증 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/auth")
public class AdminAuthController {

    /** 관리자 인증 서비스. */
    private final AdminAuthService adminAuthService;

    /** Redis 관리자 인증 토큰 서비스. */
    private final AdminRedisAuthService adminRedisAuthService;

    /** Redis 인증 설정. */
    private final AuthRedisProperties authRedisProperties;

    public AdminAuthController(
            AdminAuthService adminAuthService,
            AdminRedisAuthService adminRedisAuthService,
            AuthRedisProperties authRedisProperties
    ) {
        this.adminAuthService = adminAuthService;
        this.adminRedisAuthService = adminRedisAuthService;
        this.authRedisProperties = authRedisProperties;
    }

    /**
     * 관리자 로그인을 처리하고 Redis 인증 토큰 쿠키를 발급한다.
     */
    @PostMapping("/login")
    public ResultData setAdminLogin(@RequestBody AdminLoginRequest request, HttpServletResponse response) {
        AdminSessionVO admin = adminAuthService.setAdminLogin(request);
        String token = adminRedisAuthService.setAdminToken(admin);
        setAuthCookie(response, token, authRedisProperties.getTokenTtlSeconds());
        return ResultData.success(admin);
    }

    /**
     * 관리자 로그아웃을 처리하고 Redis 인증 토큰을 삭제한다.
     */
    @PostMapping("/logout")
    public ResultData delAdminSession(HttpServletRequest request, HttpServletResponse response) {
        adminRedisAuthService.delAdminToken(request);
        setAuthCookie(response, "", 0);
        return ResultData.success();
    }

    /**
     * Redis에 저장된 현재 관리자 인증 정보를 조회한다.
     */
    @GetMapping("/me")
    public ResultData getAdminSessionDtl(HttpServletRequest request) {
        AdminSessionVO admin = adminRedisAuthService.getAdminSessionDtl(request);
        if (admin == null) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }
        return ResultData.success(admin);
    }

    /**
     * 관리자 인증 토큰 쿠키를 응답에 추가한다.
     */
    private void setAuthCookie(HttpServletResponse response, String token, long maxAge) {
        response.addHeader("Set-Cookie", String.format(
                "%s=%s; Path=/; HttpOnly; SameSite=Lax; Max-Age=%d",
                authRedisProperties.getCookieName(),
                token,
                maxAge
        ));
    }
}

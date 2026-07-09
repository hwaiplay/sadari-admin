package org.sadari.admin.sadariadmin.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sadari.admin.sadariadmin.admin.service.AdminAuthService;
import org.sadari.admin.sadariadmin.admin.service.AdminRedisAuthService;
import org.sadari.admin.sadariadmin.admin.vo.AdminLoginRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.AuthConstant;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.config.AuthRedisProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 인증 API 컨트롤러
 */
@RestController
@RequestMapping(Constant.API_AUTH_PREFIX)
public class AdminAuthController {

    /** 관리자 인증 서비스 */
    private final AdminAuthService adminAuthService;

    /** Redis 관리자 인증 토큰 서비스 */
    private final AdminRedisAuthService adminRedisAuthService;

    /** Redis 인증 설정 */
    private final AuthRedisProperties authRedisProperties;

    /**
     * 관리자 인증 API 컨트롤러 생성
     * @Author SeungHyeon.Kang
     * @param adminAuthService
     * @param adminRedisAuthService
     * @param authRedisProperties
     * @return
     */
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
     * 관리자 로그인
     * @Author SeungHyeon.Kang
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/login")
    public ResultData setAdminLogin(@RequestBody AdminLoginRequest request, HttpServletResponse response) {
        AdminSessionVO admin = adminAuthService.setAdminLogin(request);
        String token = adminRedisAuthService.setAdminToken(admin);
        setAuthCookie(response, token, authRedisProperties.getTokenTtlSeconds());
        return ResultData.success(admin);
    }

    /**
     * 관리자 로그아웃
     * @Author SeungHyeon.Kang
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/logout")
    public ResultData delAdminSession(HttpServletRequest request, HttpServletResponse response) {
        adminRedisAuthService.delAdminToken(request);
        setAuthCookie(response, AuthConstant.EMPTY_TOKEN, AuthConstant.COOKIE_DELETE_MAX_AGE);
        return ResultData.success();
    }

    /**
     * 관리자 세션 조회
     * @Author SeungHyeon.Kang
     * @param request
     * @return
     */
    @GetMapping("/me")
    public ResultData getAdminSessionDtl(HttpServletRequest request) {
        AdminSessionVO admin = adminRedisAuthService.getAdminSessionDtl(request);
        // Redis 토큰으로 관리자 정보를 찾지 못한 경우 인증 실패 응답으로 분기한다
        if (StringUtil.isEmpty(admin)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }
        return ResultData.success(admin);
    }

    /**
     * 관리자 인증 쿠키 설정
     * @Author SeungHyeon.Kang
     * @param response
     * @param token
     * @param maxAge
     * @return
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

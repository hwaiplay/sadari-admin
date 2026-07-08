package org.sadari.admin.sadariadmin.admin.service;

import jakarta.servlet.http.HttpServletRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;

/**
 * Redis 기반 관리자 인증 토큰 서비스.
 */
public interface AdminRedisAuthService {

    /**
     * 관리자 정보를 Redis에 저장하고 인증 토큰을 발급한다.
     */
    String setAdminToken(AdminSessionVO admin);

    /**
     * 요청 쿠키의 토큰으로 Redis 관리자 정보를 조회한다.
     */
    AdminSessionVO getAdminSessionDtl(HttpServletRequest request);

    /**
     * 요청 쿠키의 토큰 정보를 Redis에서 삭제한다.
     */
    void delAdminToken(HttpServletRequest request);
}

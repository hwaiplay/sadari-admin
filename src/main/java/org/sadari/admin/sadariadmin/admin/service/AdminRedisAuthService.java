package org.sadari.admin.sadariadmin.admin.service;

import jakarta.servlet.http.HttpServletRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;

/**
 * Redis 기반 관리자 인증 토큰 서비스
 */
public interface AdminRedisAuthService {

    /**
     * 관리자 인증 토큰 저장
     * @Author SeungHyeon.Kang
     * @param admin
     * @return
     */
    String setAdminToken(AdminSessionVO admin);

    /**
     * 관리자 세션 조회
     * @Author SeungHyeon.Kang
     * @param request
     * @return
     */
    AdminSessionVO getAdminSessionDtl(HttpServletRequest request);

    /**
     * 관리자 인증 토큰 삭제
     * @Author SeungHyeon.Kang
     * @param request
     * @return
     */
    void delAdminToken(HttpServletRequest request);
}

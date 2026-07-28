package org.sadari.admin.sadariadmin.admin.service;

import jakarta.servlet.http.HttpServletRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;

/**
 * fileName       : AdminRedisAuthService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : Redis 기반 관리자 인증 토큰 서비스 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 */
public interface AdminRedisAuthService {

    /**
     * 관리자 인증 토큰 저장
     * @author SeungHyeon.Kang
     * @param admin
     * @return
     */
    String setAdminToken(AdminSessionVO admin);

    /**
     * 관리자 세션 조회
     * @author SeungHyeon.Kang
     * @param request
     * @return
     */
    AdminSessionVO getAdminSessionDtl(HttpServletRequest request);

    /**
     * 관리자 인증 토큰 삭제
     * @author SeungHyeon.Kang
     * @param request
     * @return
     */
    void delAdminToken(HttpServletRequest request);
}

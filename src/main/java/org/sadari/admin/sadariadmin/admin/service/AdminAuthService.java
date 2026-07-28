package org.sadari.admin.sadariadmin.admin.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminLoginRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;

/**
 * fileName       : AdminAuthService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 관리자 인증 서비스 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 */
public interface AdminAuthService {

    /**
     * 관리자 로그인 처리
     * @author SeungHyeon.Kang
     * @param request
     * @return
     */
    AdminSessionVO setAdminLogin(AdminLoginRequest request);
}

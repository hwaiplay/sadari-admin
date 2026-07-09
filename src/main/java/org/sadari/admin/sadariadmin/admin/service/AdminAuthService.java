package org.sadari.admin.sadariadmin.admin.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminLoginRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;

/**
 * 관리자 인증 서비스
 */
public interface AdminAuthService {

    /**
     * 관리자 로그인 처리
     * @Author SeungHyeon.Kang
     * @param request
     * @return
     */
    AdminSessionVO setAdminLogin(AdminLoginRequest request);
}

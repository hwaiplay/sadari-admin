package org.sadari.admin.sadariadmin.admin.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminLoginRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;

/**
 * 관리자 인증 서비스.
 */
public interface AdminAuthService {

    /**
     * 관리자 로그인 요청을 검증하고 Redis 저장용 관리자 정보를 반환한다.
     */
    AdminSessionVO setAdminLogin(AdminLoginRequest request);
}

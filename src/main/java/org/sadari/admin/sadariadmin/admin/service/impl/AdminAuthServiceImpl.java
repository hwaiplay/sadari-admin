package org.sadari.admin.sadariadmin.admin.service.impl;

import org.sadari.admin.sadariadmin.admin.mapper.AdminMapper;
import org.sadari.admin.sadariadmin.admin.service.AdminAuthService;
import org.sadari.admin.sadariadmin.admin.vo.AdminLoginRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.admin.vo.AdminVO;
import org.sadari.admin.sadariadmin.common.PasswordHash;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 관리자 인증 서비스 구현체.
 */
@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    /** 관리자 데이터 접근 Mapper. */
    private final AdminMapper adminMapper;

    public AdminAuthServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    /**
     * 관리자 로그인 요청을 처리한다.
     */
    @Override
    @Transactional
    public AdminSessionVO setAdminLogin(AdminLoginRequest request) {
        if (request.getAdmnIdxx() == null || request.getPassWord() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "관리자 아이디와 비밀번호를 입력해 주세요.");
        }

        AdminVO admin = adminMapper.getAdminDtl(request.getAdmnIdxx());
        if (admin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!PasswordHash.sha256(request.getPassWord()).equalsIgnoreCase(admin.getPassWord())) {
            adminMapper.uptAdminLoginFail(admin.getAdmnNumb());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (admin.getAuthLevel() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한 코드가 올바르지 않습니다.");
        }

        adminMapper.uptAdminLoginSuccess(admin.getAdmnNumb());

        AdminSessionVO session = new AdminSessionVO();
        session.setAdmnNumb(admin.getAdmnNumb());
        session.setAdmnIdxx(admin.getAdmnIdxx());
        session.setAdmnName(admin.getAdmnName());
        session.setAuthCode(admin.getAuthCode());
        session.setAuthLevel(admin.getAuthLevel());
        session.setDeptCode(admin.getDeptCode());
        return session;
    }
}

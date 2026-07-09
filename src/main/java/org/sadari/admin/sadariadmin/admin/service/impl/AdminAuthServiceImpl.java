package org.sadari.admin.sadariadmin.admin.service.impl;

import org.sadari.admin.sadariadmin.admin.mapper.AdminMapper;
import org.sadari.admin.sadariadmin.admin.service.AdminAuthService;
import org.sadari.admin.sadariadmin.admin.vo.AdminLoginRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.admin.vo.AdminVO;
import org.sadari.admin.sadariadmin.common.PasswordHash;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 인증 서비스 구현체
 */
@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    /** 관리자 데이터 접근 Mapper */
    private final AdminMapper adminMapper;

    /**
     * 관리자 인증 서비스 생성
     * @Author SeungHyeon.Kang
     * @param adminMapper
     * @return
     */
    public AdminAuthServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    /**
     * 관리자 로그인 처리
     * @Author SeungHyeon.Kang
     * @param request
     * @return
     */
    @Override
    @Transactional
    public AdminSessionVO setAdminLogin(AdminLoginRequest request) {
        if (StringUtil.isEmpty(request.getAdmnIdxx()) || StringUtil.isEmpty(request.getPassWord())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.AUTH_INVALID_REQUEST);
        }

        AdminVO admin = adminMapper.getAdminDtl(request.getAdmnIdxx());
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_INVALID_CREDENTIALS);
        }

        if (!PasswordHash.sha256(request.getPassWord()).equalsIgnoreCase(admin.getPassWord())) {
            adminMapper.uptAdminLoginFail(admin.getAdmnNumb());
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_INVALID_CREDENTIALS);
        }

        if (StringUtil.isEmpty(admin.getAuthLevel())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, ResultEnum.AUTH_INVALID_CODE);
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


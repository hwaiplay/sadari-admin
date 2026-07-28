package org.sadari.admin.sadariadmin.adminauth.service.impl;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.adminauth.mapper.AdminAuthMapper;
import org.sadari.admin.sadariadmin.adminauth.service.AdminAuthManageService;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthManageVO;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthVO;
import org.sadari.admin.sadariadmin.authgroup.mapper.AuthGroupMapper;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * fileName       : AdminAuthManageServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 관리자 권한 부여 서비스 구현체
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@Service
@Transactional(readOnly = true)
public class AdminAuthManageServiceImpl implements AdminAuthManageService {

    /** 관리자 권한 Mapper */
    private final AdminAuthMapper adminAuthMapper;

    /** 권한그룹 Mapper */
    private final AuthGroupMapper authGroupMapper;

    /** 관리자 권한 부여 서비스 생성 */
    public AdminAuthManageServiceImpl(AdminAuthMapper adminAuthMapper, AuthGroupMapper authGroupMapper) {
        this.adminAuthMapper = adminAuthMapper;
        this.authGroupMapper = authGroupMapper;
    }

    /** 관리자 권한 부여 화면 데이터 조회 */
    @Override
    public AdminAuthManageVO getAdminAuthManage(int pageNumber, AdminSessionVO admin) {
        checkLogin(admin);
        PageRequest pageRequest = new PageRequest(pageNumber);
        AdminAuthManageVO result = new AdminAuthManageVO();
        result.setAdmins(PageData.of(adminAuthMapper.getAdminAuthList(pageRequest.getStartRow(), pageRequest.getEndRow())
                                   , adminAuthMapper.getAdminAuthListCount(), pageRequest));
        result.setAuthGroups(authGroupMapper.getAuthGroupList(1, Integer.MAX_VALUE));
        return result;
    }

    /** 관리자 권한 일괄 수정 */
    @Override
    @Transactional
    public AdminAuthManageVO uptAdminAuthList(List<AdminAuthVO> admins, int pageNumber, AdminSessionVO admin) {
        checkLogin(admin);
        if (StringUtil.isEmpty(admins)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
        for (AdminAuthVO target : admins) {
            if (StringUtil.isEmpty(target.getAdmnNumb()) || StringUtil.isEmpty(target.getAuthCode())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
            }
            if (adminAuthMapper.getAdminCount(target.getAdmnNumb()) == 0) {
                throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.ADMIN_NOT_FOUND);
            }
            if (adminAuthMapper.getAuthGroupCount(target.getAuthCode()) == 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.AUTH_GROUP_NOT_FOUND);
            }
            adminAuthMapper.uptAdminAuth(target);
        }
        return getAdminAuthManage(pageNumber, admin);
    }

    /** 로그인 상태 확인 */
    private void checkLogin(AdminSessionVO admin) {
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}

package org.sadari.admin.sadariadmin.adminauth.service.impl;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.adminauth.mapper.AdminAuthMapper;
import org.sadari.admin.sadariadmin.adminauth.service.AdminAuthManageService;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthManageVO;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthSearchVO;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthVO;
import org.sadari.admin.sadariadmin.authgroup.mapper.AuthGroupMapper;
import org.sadari.admin.sadariadmin.authgroup.vo.AuthGroupSearchVO;
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
 * 2026-07-31        SeungHyeon.Kang    관리자 권한 목록 검색 조건 추가
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
    public AdminAuthManageVO getAdminAuthManage(AdminAuthSearchVO search, AdminSessionVO admin) {
        // 인증되지 않은 요청이 관리자 권한 정보를 조회하지 못하도록 로그인 상태를 확인한다
        checkLogin(admin);
        // 요청 페이지에 해당하는 조회 행 범위를 계산한다
        PageRequest pageRequest = new PageRequest(search.getPage());
        // 목록과 건수 조회에 같은 검색 조건과 시작 행을 적용한다
        search.setStartRow(pageRequest.getStartRow());
        // 검색 조건에 페이지 마지막 행을 적용한다
        search.setEndRow(pageRequest.getEndRow());
        // 관리자 권한 목록과 권한그룹 선택 항목을 담을 결과를 생성한다
        AdminAuthManageVO result = new AdminAuthManageVO();
        // 검색 조건에 맞는 관리자 목록과 전체 건수를 결과에 설정한다
        result.setAdmins(PageData.of(adminAuthMapper.getAdminAuthList(search)
                                   , adminAuthMapper.getAdminAuthListCount(search), pageRequest));
        // 권한그룹 선택 항목 전체를 조회할 검색 범위를 생성한다
        AuthGroupSearchVO authGroupSearch = new AuthGroupSearchVO();
        // 권한그룹 선택 항목을 첫 행부터 조회한다
        authGroupSearch.setStartRow(1);
        // 관리 가능한 모든 권한그룹을 한 번에 조회한다
        authGroupSearch.setEndRow(Integer.MAX_VALUE);
        // 관리자 권한 필터와 수정 선택 항목에 사용할 권한그룹을 설정한다
        result.setAuthGroups(authGroupMapper.getAuthGroupList(authGroupSearch));
        // 관리자 권한 화면 데이터를 반환한다
        return result;
    }

    /** 관리자 권한 일괄 수정 */
    @Override
    @Transactional
    public AdminAuthManageVO uptAdminAuthList(List<AdminAuthVO> admins, AdminAuthSearchVO search
                                           , AdminSessionVO admin) {
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
        // 수정 후에도 현재 검색 조건과 페이지를 유지한 화면 데이터를 반환한다
        return getAdminAuthManage(search, admin);
    }

    /** 로그인 상태 확인 */
    private void checkLogin(AdminSessionVO admin) {
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}

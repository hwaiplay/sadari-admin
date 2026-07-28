package org.sadari.admin.sadariadmin.authgroup.service.impl;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.authgroup.mapper.AuthGroupMapper;
import org.sadari.admin.sadariadmin.authgroup.service.AuthGroupService;
import org.sadari.admin.sadariadmin.authgroup.vo.AuthGroupVO;
import org.sadari.admin.sadariadmin.authgroup.vo.AuthMenuVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
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
 * fileName       : AuthGroupServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 권한그룹 관리 서비스 구현체
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@Service
@Transactional(readOnly = true)
public class AuthGroupServiceImpl implements AuthGroupService {

    /** 권한그룹 Mapper */
    private final AuthGroupMapper authGroupMapper;

    /**
     * 권한그룹 관리 서비스 생성
     * @author SeungHyeon.Kang
     * @param authGroupMapper
     * @return
     */
    public AuthGroupServiceImpl(AuthGroupMapper authGroupMapper) {
        this.authGroupMapper = authGroupMapper;
    }

    /** 권한그룹 목록 조회 */
    @Override
    public PageData<AuthGroupVO> getAuthGroupList(int pageNumber, AdminSessionVO admin) {
        checkLogin(admin);
        PageRequest pageRequest = new PageRequest(pageNumber);
        return PageData.of(authGroupMapper.getAuthGroupList(pageRequest.getStartRow(), pageRequest.getEndRow())
                         , authGroupMapper.getAuthGroupListCount(), pageRequest);
    }

    /** 권한그룹 상세 조회 */
    @Override
    public AuthGroupVO getAuthGroup(String authCode, AdminSessionVO admin) {
        checkLogin(admin);
        AuthGroupVO authGroup = authGroupMapper.getAuthGroup(authCode);
        if (StringUtil.isEmpty(authGroup)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.AUTH_GROUP_NOT_FOUND);
        }
        authGroup.setMenus(authGroupMapper.getAuthMenuList(authCode));
        return authGroup;
    }

    /** 권한 코드 중복 확인 */
    @Override
    public boolean isDuplicate(String authCode, AdminSessionVO admin) {
        checkLogin(admin);
        return !StringUtil.isEmpty(authCode) && authGroupMapper.getAuthGroupCount(authCode) > 0;
    }

    /** 권한그룹 등록 */
    @Override
    @Transactional
    public AuthGroupVO setAuthGroup(AuthGroupVO authGroup, AdminSessionVO admin) {
        checkLogin(admin);
        validate(authGroup);
        if (authGroupMapper.getAuthGroupCount(authGroup.getAuthCode()) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.AUTH_GROUP_DUPLICATE);
        }
        setAudit(authGroup, admin);
        authGroupMapper.setAuthGroup(authGroup);
        saveMenus(authGroup, admin);
        return getAuthGroup(authGroup.getAuthCode(), admin);
    }

    /** 권한그룹 수정 */
    @Override
    @Transactional
    public AuthGroupVO uptAuthGroup(AuthGroupVO authGroup, AdminSessionVO admin) {
        checkLogin(admin);
        validate(authGroup);
        if (authGroupMapper.getAuthGroupCount(authGroup.getAuthCode()) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.AUTH_GROUP_NOT_FOUND);
        }
        authGroup.setUpdtAdmn(admin.getAdmnNumb());
        authGroupMapper.uptAuthGroup(authGroup);
        authGroupMapper.delAuthMenu(authGroup.getAuthCode());
        saveMenus(authGroup, admin);
        return getAuthGroup(authGroup.getAuthCode(), admin);
    }

    /** 권한그룹 삭제 */
    @Override
    @Transactional
    public void delAuthGroup(String authCode, AdminSessionVO admin) {
        checkLogin(admin);
        authGroupMapper.delAuthMenu(authCode);
        authGroupMapper.delAuthGroup(authCode);
    }

    /** 메뉴 권한 일괄 저장 */
    private void saveMenus(AuthGroupVO authGroup, AdminSessionVO admin) {
        if (StringUtil.isEmpty(authGroup.getMenus())) {
            return;
        }
        for (AuthMenuVO menu : authGroup.getMenus()) {
            menu.setAuthCode(authGroup.getAuthCode());
            menu.setReadYsno(toYsno(menu.getReadYsno()));
            menu.setWritYsno(toYsno(menu.getWritYsno()));
            menu.setDeltYsno(toYsno(menu.getDeltYsno()));
            menu.setRegiAdmn(admin.getAdmnNumb());
            menu.setUpdtAdmn(admin.getAdmnNumb());
            authGroupMapper.setAuthMenu(menu);
        }
    }

    /** 권한그룹 입력값 검증 */
    private void validate(AuthGroupVO authGroup) {
        if (StringUtil.isEmpty(authGroup)
                || StringUtil.isEmpty(authGroup.getAuthCode())
                || StringUtil.isEmpty(authGroup.getAuthName())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
        authGroup.setAuthCode(authGroup.getAuthCode().trim().toUpperCase());
        authGroup.setAuthName(authGroup.getAuthName().trim());
        authGroup.setUseeYsno(toUseeYsno(authGroup.getUseeYsno()));
    }

    /** 감사 정보 설정 */
    private void setAudit(AuthGroupVO authGroup, AdminSessionVO admin) {
        authGroup.setRegiAdmn(admin.getAdmnNumb());
        authGroup.setUpdtAdmn(admin.getAdmnNumb());
    }

    /** Y 또는 N 값 정규화 */
    private String toYsno(String value) {
        return Constant.YES.equals(value) ? Constant.YES : Constant.NO;
    }

    /**
     * 권한그룹 사용 여부 기본값 설정
     * @author SeungHyeon.Kang
     * @param value
     * @return
     */
    private String toUseeYsno(String value) {
        return StringUtil.isEmpty(value) || Constant.YES.equals(value) ? Constant.YES : Constant.NO;
    }

    /** 로그인 상태 확인 */
    private void checkLogin(AdminSessionVO admin) {
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}

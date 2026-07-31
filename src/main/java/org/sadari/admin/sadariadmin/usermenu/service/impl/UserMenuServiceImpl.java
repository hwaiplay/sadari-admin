package org.sadari.admin.sadariadmin.usermenu.service.impl;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.usermenu.mapper.UserMenuMapper;
import org.sadari.admin.sadariadmin.usermenu.service.UserMenuService;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuSearchVO;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * fileName       : UserMenuServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 관리 서비스 구현체
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    사용자 메뉴 목록 검색 조건 추가
 */
@Service
@Transactional(readOnly = true)
public class UserMenuServiceImpl implements UserMenuService {

    /** 사용자 메뉴 Mapper */
    private final UserMenuMapper userMenuMapper;

    /** 사용자 메뉴 관리 서비스 생성 */
    public UserMenuServiceImpl(UserMenuMapper userMenuMapper) {
        this.userMenuMapper = userMenuMapper;
    }

    /** 사용자 상위 메뉴 목록 조회 */
    @Override
    public PageData<UserMenuVO> getUserMenuList(UserMenuSearchVO search, AdminSessionVO admin) {
        // 인증되지 않은 요청이 사용자 메뉴 정보를 조회하지 못하도록 로그인 상태를 확인한다
        checkLogin(admin);
        // 요청 페이지에 해당하는 조회 행 범위를 계산한다
        PageRequest pageRequest = new PageRequest(search.getPage());
        // 목록과 건수 조회에 같은 검색 조건과 시작 행을 적용한다
        search.setStartRow(pageRequest.getStartRow());
        // 검색 조건에 페이지 마지막 행을 적용한다
        search.setEndRow(pageRequest.getEndRow());
        // 검색 조건에 맞는 사용자 메뉴 목록과 전체 건수로 페이지 응답을 생성한다
        return PageData.of(userMenuMapper.getUserMenuList(search), userMenuMapper.getUserMenuCount(search)
                         , pageRequest);
    }

    /** 사용자 메뉴 상세 조회 */
    @Override
    public UserMenuVO getUserMenuDtl(String menuNumb, String subxNumb, AdminSessionVO admin) {
        checkLogin(admin);
        UserMenuVO menu = userMenuMapper.getUserMenuDtl(menuNumb, subxNumb);
        // 복합키에 해당하는 사용자 메뉴가 없으면 조회 결과 없음으로 분기한다
        if (StringUtil.isEmpty(menu)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.MENU_NOT_FOUND);
        }
        return menu;
    }

    /** 사용자 하위 메뉴 목록 조회 */
    @Override
    public List<UserMenuVO> getUserSubMenuList(String menuNumb, AdminSessionVO admin) {
        checkLogin(admin);
        return userMenuMapper.getUserSubMenuList(menuNumb);
    }

    /** 사용자 메뉴 등록 */
    @Override
    @Transactional
    public UserMenuVO setUserMenu(UserMenuVO menu, AdminSessionVO admin) {
        checkLogin(admin);
        checkRequired(menu);
        setMenuKey(menu);
        setDefaults(menu, admin);
        userMenuMapper.setUserMenu(menu);
        return userMenuMapper.getUserMenuDtl(menu.getMenuNumb(), menu.getSubxNumb());
    }

    /** 사용자 메뉴 수정 */
    @Override
    @Transactional
    public UserMenuVO uptUserMenu(UserMenuVO menu, AdminSessionVO admin) {
        checkLogin(admin);
        checkRequired(menu);
        setDefaults(menu, admin);
        userMenuMapper.uptUserMenu(menu);
        return userMenuMapper.getUserMenuDtl(menu.getMenuNumb(), menu.getSubxNumb());
    }

    /** 사용자 메뉴 삭제 */
    @Override
    @Transactional
    public void delUserMenu(String menuNumb, String subxNumb, AdminSessionVO admin) {
        checkLogin(admin);
        userMenuMapper.delUserMenu(menuNumb, subxNumb);
    }

    /** 사용자 메뉴 필수값 확인 */
    private void checkRequired(UserMenuVO menu) {
        // 메뉴명 또는 URL이 없으면 저장할 수 없는 요청으로 분기한다
        if (StringUtil.isEmpty(menu) || StringUtil.isEmpty(menu.getMenuName()) || StringUtil.isEmpty(menu.getMenuUrlx())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
    }

    /** 사용자 메뉴 복합키 생성 */
    private void setMenuKey(UserMenuVO menu) {
        // 메뉴 번호가 없으면 상위 메뉴 등록으로 판단한다
        if (StringUtil.isEmpty(menu.getMenuNumb())) {
            menu.setMenuNumb(userMenuMapper.getUserMenuNumb());
            menu.setSubxNumb(Constant.TOP_MENU_SUBX_NUMB);
            return;
        }
        // 메뉴 번호가 있고 하위 메뉴 번호가 없으면 하위 메뉴 등록으로 판단한다
        if (StringUtil.isEmpty(menu.getSubxNumb())) {
            menu.setSubxNumb(userMenuMapper.getUserSubxNumb(menu.getMenuNumb()));
        }
    }

    /** 사용자 메뉴 기본값 설정 */
    private void setDefaults(UserMenuVO menu, AdminSessionVO admin) {
        if (StringUtil.isEmpty(menu.getShowYsno())) {
            menu.setShowYsno(Constant.YES);
        }
        // 햄버거 메뉴에 노출하는 경우에만 정렬 순서를 저장한다
        if (Constant.YES.equals(menu.getShowYsno())
                && (StringUtil.isEmpty(menu.getSortOrdr()) || menu.getSortOrdr() < Constant.DEFAULT_MENU_SORT_ORDR)) {
            menu.setSortOrdr(Constant.DEFAULT_MENU_SORT_ORDR);
        } else if (Constant.NO.equals(menu.getShowYsno())) {
            menu.setSortOrdr(null);
        }
        if (StringUtil.isEmpty(menu.getUseeYsno())) {
            menu.setUseeYsno(Constant.YES);
        }
        menu.setRegiAdmn(admin.getAdmnNumb());
        menu.setUpdtAdmn(admin.getAdmnNumb());
    }

    /** 로그인 상태 확인 */
    private void checkLogin(AdminSessionVO admin) {
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}

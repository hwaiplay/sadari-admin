package org.sadari.admin.sadariadmin.menu.service.impl;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.menu.mapper.MenuMapper;
import org.sadari.admin.sadariadmin.menu.service.MenuService;
import org.sadari.admin.sadariadmin.menu.vo.MenuVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 메뉴 관리 서비스 구현체 */
@Service
public class MenuServiceImpl implements MenuService {

    /** 관리자 메뉴 Mapper */
    private final MenuMapper menuMapper;

    /**
     * 메뉴 관리 서비스 생성
     * @Author SeungHyeon.Kang
     * @param menuMapper
     * @return
     */
    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    /**
     * 권한 레벨별 사이드바 메뉴 목록 조회
     * @Author SeungHyeon.Kang
     * @param authLevel
     * @return
     */
    @Override
    public List<MenuVO> getMenuList(Integer authLevel) {
        return menuMapper.getMenuList(authLevel);
    }

    /**
     * 메뉴관리 목록 조회
     * @Author SeungHyeon.Kang
     * @param admin
     * @return
     */
    @Override
    public List<MenuVO> getMenuMngList(AdminSessionVO admin) {
        checkLogin(admin);
        return menuMapper.getMenuMngList();
    }

    /**
     * 메뉴 상세 조회
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @param admin
     * @return
     */
    @Override
    public MenuVO getMenuDtl(String menuNumb, String subxNumb, AdminSessionVO admin) {
        checkLogin(admin);
        MenuVO menu = menuMapper.getMenuDtl(menuNumb, subxNumb);
        // 복합키에 해당하는 메뉴가 없으면 조회 결과 없음으로 분기한다
        if (StringUtil.isEmpty(menu)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.MENU_NOT_FOUND);
        }
        return menu;
    }

    /**
     * 하위 메뉴 목록 조회
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param admin
     * @return
     */
    @Override
    public List<MenuVO> getSubMenuList(String menuNumb, AdminSessionVO admin) {
        checkLogin(admin);
        return menuMapper.getSubMenuList(menuNumb);
    }

    /**
     * 메뉴 등록
     * @Author SeungHyeon.Kang
     * @param menu
     * @param admin
     * @return
     */
    @Override
    @Transactional
    public MenuVO setMenu(MenuVO menu, AdminSessionVO admin) {
        checkLogin(admin);
        setMenuKey(menu);
        setMenuDefault(menu, admin);
        menuMapper.setMenu(menu);
        return menuMapper.getMenuDtl(menu.getMenuNumb(), menu.getSubxNumb());
    }

    /**
     * 메뉴 수정
     * @Author SeungHyeon.Kang
     * @param menu
     * @param admin
     * @return
     */
    @Override
    @Transactional
    public MenuVO uptMenu(MenuVO menu, AdminSessionVO admin) {
        checkLogin(admin);
        setMenuDefault(menu, admin);
        menu.setUpdtAdmn(admin.getAdmnNumb());
        menuMapper.uptMenu(menu);
        return menuMapper.getMenuDtl(menu.getMenuNumb(), menu.getSubxNumb());
    }

    /**
     * 메뉴 삭제
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @param admin
     * @return
     */
    @Override
    @Transactional
    public void delMenu(String menuNumb, String subxNumb, AdminSessionVO admin) {
        checkLogin(admin);
        menuMapper.delMenu(menuNumb, subxNumb);
    }

    /**
     * 메뉴 복합키 생성
     * @Author SeungHyeon.Kang
     * @param menu
     * @return
     */
    private void setMenuKey(MenuVO menu) {
        // MENU_NUMB가 없으면 상위 메뉴 등록으로 판단하고 신규 상위 메뉴 번호를 생성한다
        if (StringUtil.isEmpty(menu.getMenuNumb())) {
            menu.setMenuNumb(menuMapper.getMenuNumb());
            menu.setSubxNumb(Constant.TOP_MENU_SUBX_NUMB);
            return;
        }

        // MENU_NUMB가 있고 SUBX_NUMB가 없으면 하위 메뉴 등록으로 판단하고 신규 하위 메뉴 번호를 생성한다
        if (StringUtil.isEmpty(menu.getSubxNumb())) {
            menu.setSubxNumb(menuMapper.getSubxNumb(menu.getMenuNumb()));
        }
    }

    /**
     * 메뉴 기본값 설정
     * @Author SeungHyeon.Kang
     * @param menu
     * @param admin
     * @return
     */
    private void setMenuDefault(MenuVO menu, AdminSessionVO admin) {
        // 조회 권한이 없으면 로그인 관리자의 권한을 기본 권한으로 사용한다
        if (StringUtil.isEmpty(menu.getReadAuth())) {
            menu.setReadAuth(admin.getAuthCode());
        }
        // 쓰기 권한이 없으면 조회 권한과 동일한 권한을 사용한다
        if (StringUtil.isEmpty(menu.getWritAuth())) {
            menu.setWritAuth(menu.getReadAuth());
        }
        // 삭제 권한이 없으면 조회 권한과 동일한 권한을 사용한다
        if (StringUtil.isEmpty(menu.getDeltAuth())) {
            menu.setDeltAuth(menu.getReadAuth());
        }
        // 사용 여부가 없으면 사용 상태로 저장한다
        if (StringUtil.isEmpty(menu.getUseeYsno())) {
            menu.setUseeYsno(Constant.YES);
        }
        // 정렬 순서가 없으면 시스템 기본 정렬 순서를 사용한다
        if (StringUtil.isEmpty(menu.getSortOrdr())) {
            menu.setSortOrdr(Constant.DEFAULT_MENU_SORT_ORDR);
        }
        menu.setRegiAdmn(admin.getAdmnNumb());
        menu.setUpdtAdmn(admin.getAdmnNumb());
    }

    /**
     * 로그인 상태 확인
     * @Author SeungHyeon.Kang
     * @param admin
     * @return
     */
    private void checkLogin(AdminSessionVO admin) {
        // 인증 객체가 없으면 로그인하지 않은 요청으로 판단한다
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}

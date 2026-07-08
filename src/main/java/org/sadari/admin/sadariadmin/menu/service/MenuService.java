package org.sadari.admin.sadariadmin.menu.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.menu.vo.MenuVO;

import java.util.List;

/** 메뉴 관리 서비스. */
public interface MenuService {

    /** 권한 레벨별 사이드바 메뉴 목록을 조회한다. */
    List<MenuVO> getMenuList(Integer authLevel);

    /** 메뉴관리 목록을 조회한다. */
    List<MenuVO> getMenuMngList(AdminSessionVO admin);

    /** 메뉴 상세를 조회한다. */
    MenuVO getMenuDtl(String menuNumb, String subxNumb, AdminSessionVO admin);

    /** 하위 메뉴 목록을 조회한다. */
    List<MenuVO> getSubMenuList(String menuNumb, AdminSessionVO admin);

    /** 메뉴를 등록한다. */
    MenuVO setMenu(MenuVO menu, AdminSessionVO admin);

    /** 메뉴를 수정한다. */
    MenuVO uptMenu(MenuVO menu, AdminSessionVO admin);

    /** 메뉴를 삭제한다. */
    void delMenu(String menuNumb, String subxNumb, AdminSessionVO admin);
}

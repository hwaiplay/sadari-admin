package org.sadari.admin.sadariadmin.menu.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.menu.vo.MenuVO;

import java.util.List;

/** 메뉴 관리 서비스 */
public interface MenuService {

    /**
     * 권한 레벨별 사이드바 메뉴 목록 조회
     * @Author SeungHyeon.Kang
     * @param authLevel
     * @return
     */
    List<MenuVO> getMenuList(Integer authLevel);

    /**
     * 메뉴관리 목록 조회
     * @Author SeungHyeon.Kang
     * @param admin
     * @return
     */
    List<MenuVO> getMenuMngList(AdminSessionVO admin);

    /**
     * 메뉴 상세 조회
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @param admin
     * @return
     */
    MenuVO getMenuDtl(String menuNumb, String subxNumb, AdminSessionVO admin);

    /**
     * 하위 메뉴 목록 조회
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param admin
     * @return
     */
    List<MenuVO> getSubMenuList(String menuNumb, AdminSessionVO admin);

    /**
     * 메뉴 등록
     * @Author SeungHyeon.Kang
     * @param menu
     * @param admin
     * @return
     */
    MenuVO setMenu(MenuVO menu, AdminSessionVO admin);

    /**
     * 메뉴 수정
     * @Author SeungHyeon.Kang
     * @param menu
     * @param admin
     * @return
     */
    MenuVO uptMenu(MenuVO menu, AdminSessionVO admin);

    /**
     * 메뉴 삭제
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @param admin
     * @return
     */
    void delMenu(String menuNumb, String subxNumb, AdminSessionVO admin);
}

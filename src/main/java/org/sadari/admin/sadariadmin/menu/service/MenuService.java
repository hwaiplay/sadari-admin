package org.sadari.admin.sadariadmin.menu.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.menu.vo.MenuPermissionVO;
import org.sadari.admin.sadariadmin.menu.vo.MenuSearchVO;
import org.sadari.admin.sadariadmin.menu.vo.MenuVO;

import java.util.List;

/**
 * fileName       : MenuService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 메뉴 관리 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    메뉴 목록 검색 조건 추가
 */
public interface MenuService {

    /**
     * 권한 레벨별 사이드바 메뉴 목록 조회
     * @author SeungHyeon.Kang
     * @param authLevel
     * @return
     */
    List<MenuVO> getMenuList(AdminSessionVO admin);

    /** 관리자 메뉴 권한 조회 */
    MenuPermissionVO getMenuPermission(String menuUrlx, AdminSessionVO admin);

    /**
     * 메뉴관리 목록 조회
     * @author SeungHyeon.Kang
     * @param search 메뉴 검색 조건
     * @param admin 로그인한 관리자
     * @return 검색된 메뉴 목록
     */
    PageData<MenuVO> getMenuMngList(MenuSearchVO search, AdminSessionVO admin);

    /**
     * 메뉴 상세 조회
     * @author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @param admin
     * @return
     */
    MenuVO getMenuDtl(String menuNumb, String subxNumb, AdminSessionVO admin);

    /**
     * 하위 메뉴 목록 조회
     * @author SeungHyeon.Kang
     * @param menuNumb
     * @param admin
     * @return
     */
    List<MenuVO> getSubMenuList(String menuNumb, AdminSessionVO admin);

    /**
     * 메뉴 등록
     * @author SeungHyeon.Kang
     * @param menu
     * @param admin
     * @return
     */
    MenuVO setMenu(MenuVO menu, AdminSessionVO admin);

    /**
     * 메뉴 수정
     * @author SeungHyeon.Kang
     * @param menu
     * @param admin
     * @return
     */
    MenuVO uptMenu(MenuVO menu, AdminSessionVO admin);

    /**
     * 메뉴 삭제
     * @author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @param admin
     * @return
     */
    void delMenu(String menuNumb, String subxNumb, AdminSessionVO admin);
}

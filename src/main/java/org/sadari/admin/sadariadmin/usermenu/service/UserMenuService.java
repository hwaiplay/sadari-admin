package org.sadari.admin.sadariadmin.usermenu.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuVO;

import java.util.List;

/** 사용자 메뉴 관리 서비스 */
public interface UserMenuService {

    /** 사용자 상위 메뉴 목록 조회 */
    List<UserMenuVO> getUserMenuList(AdminSessionVO admin);

    /** 사용자 메뉴 상세 조회 */
    UserMenuVO getUserMenuDtl(String menuNumb, String subxNumb, AdminSessionVO admin);

    /** 사용자 하위 메뉴 목록 조회 */
    List<UserMenuVO> getUserSubMenuList(String menuNumb, AdminSessionVO admin);

    /** 사용자 메뉴 등록 */
    UserMenuVO setUserMenu(UserMenuVO menu, AdminSessionVO admin);

    /** 사용자 메뉴 수정 */
    UserMenuVO uptUserMenu(UserMenuVO menu, AdminSessionVO admin);

    /** 사용자 메뉴 삭제 */
    void delUserMenu(String menuNumb, String subxNumb, AdminSessionVO admin);
}

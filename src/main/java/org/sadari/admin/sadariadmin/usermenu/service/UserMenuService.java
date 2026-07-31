package org.sadari.admin.sadariadmin.usermenu.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuSearchVO;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuVO;

import java.util.List;

/**
 * fileName       : UserMenuService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 관리 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    사용자 메뉴 목록 검색 조건 추가
 */
public interface UserMenuService {

    /** 사용자 상위 메뉴 목록 조회 */
    PageData<UserMenuVO> getUserMenuList(UserMenuSearchVO search, AdminSessionVO admin);

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

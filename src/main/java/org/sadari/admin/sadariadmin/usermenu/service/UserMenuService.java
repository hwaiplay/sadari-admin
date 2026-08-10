package org.sadari.admin.sadariadmin.usermenu.service;

import java.util.List;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuSearchVO;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuVO;

/**
 * fileName       : UserMenuService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 관리 기능을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    사용자 메뉴 목록 검색 조건 추가
 * 2026-08-10        SeungHyeon.Kang    3단계 인접 목록 메뉴 구조 적용
 */
public interface UserMenuService {

    /** 사용자 메뉴 목록을 조회한다. */
    PageData<UserMenuVO> getUserMenuList(UserMenuSearchVO search, AdminSessionVO admin);

    /** 사용자 메뉴 상세를 조회한다. */
    UserMenuVO getUserMenuDtl(Long menuNumb, AdminSessionVO admin);

    /** 사용자 메뉴의 상위 메뉴 후보 목록을 조회한다. */
    List<UserMenuVO> getUserMenuParentList(AdminSessionVO admin);

    /** 사용자 메뉴를 등록한다. */
    UserMenuVO setUserMenu(UserMenuVO menu, AdminSessionVO admin);

    /** 사용자 메뉴를 수정한다. */
    UserMenuVO uptUserMenu(UserMenuVO menu, AdminSessionVO admin);

    /** 사용자 메뉴를 삭제한다. */
    void delUserMenu(Long menuNumb, AdminSessionVO admin);
}

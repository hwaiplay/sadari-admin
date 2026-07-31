package org.sadari.admin.sadariadmin.menu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.menu.vo.MenuPermissionVO;
import org.sadari.admin.sadariadmin.menu.vo.MenuSearchVO;
import org.sadari.admin.sadariadmin.menu.vo.MenuVO;

import java.util.List;

/**
 * fileName       : MenuMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : MenuMapper role
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    메뉴 목록 검색 조건 추가
 */@Mapper
public interface MenuMapper {

    /**
     * 권한 레벨별 사이드바 메뉴 목록 조회
     * @author SeungHyeon.Kang
     * @param authLevel
     * @param search 메뉴 검색 조건과 페이지 범위
     * @return 검색된 메뉴 목록
     */
    List<MenuVO> getMenuList(@Param("authCode") String authCode);

    /**
     * 관리자 메뉴 권한 조회
     * @author SeungHyeon.Kang
     * @param authCode
     * @param menuUrlx
     * @return
     */
    MenuPermissionVO getMenuPermission(@Param("authCode") String authCode, @Param("menuUrlx") String menuUrlx);

    /**
     * 메뉴관리 전체 목록 조회
     * @author SeungHyeon.Kang
     * @return
     */
    List<MenuVO> getMenuMngList(MenuSearchVO search);

    /**
     * 검색 조건에 맞는 관리자 메뉴 전체 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 메뉴 검색 조건
     * @return 검색된 메뉴 전체 건수
     */
    int getMenuMngCount(MenuSearchVO search);

    /**
     * 메뉴 상세 조회
     * @author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @return
     */
    MenuVO getMenuDtl(@Param("menuNumb") String menuNumb, @Param("subxNumb") String subxNumb);

    /**
     * 하위 메뉴 목록 조회
     * @author SeungHyeon.Kang
     * @param menuNumb
     * @return
     */
    List<MenuVO> getSubMenuList(@Param("menuNumb") String menuNumb);

    /**
     * 신규 상위 메뉴 번호 조회
     * @author SeungHyeon.Kang
     * @return
     */
    String getMenuNumb();

    /**
     * 신규 하위 메뉴 번호 조회
     * @author SeungHyeon.Kang
     * @param menuNumb
     * @return
     */
    String getSubxNumb(@Param("menuNumb") String menuNumb);

    /**
     * 메뉴 등록
     * @author SeungHyeon.Kang
     * @param menu
     * @return
     */
    void setMenu(MenuVO menu);

    /**
     * 신규 메뉴 전체 권한그룹 권한 등록
     * @author SeungHyeon.Kang
     * @param menu
     * @return
     */
    void setMenuAuth(MenuVO menu);

    /**
     * 메뉴 수정
     * @author SeungHyeon.Kang
     * @param menu
     * @return
     */
    void uptMenu(MenuVO menu);

    /**
     * 메뉴 삭제
     * @author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @return
     */
    void delMenu(@Param("menuNumb") String menuNumb, @Param("subxNumb") String subxNumb);

    /**
     * 메뉴별 권한 삭제
     * @author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @return
     */
    void delMenuAuth(@Param("menuNumb") String menuNumb, @Param("subxNumb") String subxNumb);
}

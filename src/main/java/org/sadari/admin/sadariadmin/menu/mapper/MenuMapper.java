package org.sadari.admin.sadariadmin.menu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.menu.vo.MenuVO;
import org.sadari.admin.sadariadmin.menu.vo.MenuPermissionVO;

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
 */@Mapper
public interface MenuMapper {

    /**
     * 권한 레벨별 사이드바 메뉴 목록 조회
     * @author SeungHyeon.Kang
     * @param authLevel
     * @return
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
    List<MenuVO> getMenuMngList(@Param("startRow") int startRow, @Param("endRow") int endRow);

    int getMenuMngCount();

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

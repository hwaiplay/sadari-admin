package org.sadari.admin.sadariadmin.menu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.menu.vo.MenuPermissionVO;
import org.sadari.admin.sadariadmin.menu.vo.MenuVO;

import java.util.List;

@Mapper
public interface MenuMapper {

    /**
     * 권한 레벨별 사이드바 메뉴 목록 조회
     * @Author SeungHyeon.Kang
     * @param authLevel
     * @return
     */
    List<MenuVO> getMenuList(@Param("authLevel") Integer authLevel);

    /**
     * 메뉴 URL 기준 권한 레벨 조회
     * @Author SeungHyeon.Kang
     * @param menuUrlx
     * @return
     */
    MenuPermissionVO getMenuPermission(@Param("menuUrlx") String menuUrlx);

    /**
     * 메뉴관리 전체 목록 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    List<MenuVO> getMenuMngList();

    /**
     * 메뉴 상세 조회
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @return
     */
    MenuVO getMenuDtl(@Param("menuNumb") String menuNumb, @Param("subxNumb") String subxNumb);

    /**
     * 하위 메뉴 목록 조회
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @return
     */
    List<MenuVO> getSubMenuList(@Param("menuNumb") String menuNumb);

    /**
     * 신규 상위 메뉴 번호 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    String getMenuNumb();

    /**
     * 신규 하위 메뉴 번호 조회
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @return
     */
    String getSubxNumb(@Param("menuNumb") String menuNumb);

    /**
     * 메뉴 등록
     * @Author SeungHyeon.Kang
     * @param menu
     * @return
     */
    void setMenu(MenuVO menu);

    /**
     * 메뉴 수정
     * @Author SeungHyeon.Kang
     * @param menu
     * @return
     */
    void uptMenu(MenuVO menu);

    /**
     * 메뉴 삭제
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @return
     */
    void delMenu(@Param("menuNumb") String menuNumb, @Param("subxNumb") String subxNumb);
}

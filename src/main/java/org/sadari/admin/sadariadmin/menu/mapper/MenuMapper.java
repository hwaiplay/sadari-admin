package org.sadari.admin.sadariadmin.menu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.menu.vo.MenuPermissionVO;
import org.sadari.admin.sadariadmin.menu.vo.MenuVO;

import java.util.List;

@Mapper
public interface MenuMapper {

    /** 로그인한 관리자의 권한 레벨로 노출 가능한 사이드바 메뉴 목록을 조회한다. */
    List<MenuVO> getMenuList(@Param("authLevel") Integer authLevel);

    /** 메뉴 URL 기준 권한 레벨을 조회한다. */
    MenuPermissionVO getMenuPermission(@Param("menuUrlx") String menuUrlx);

    /** 메뉴관리 화면에 표시할 전체 메뉴 목록을 조회한다. */
    List<MenuVO> getMenuMngList();

    /** 메뉴 복합키로 메뉴 상세를 조회한다. */
    MenuVO getMenuDtl(@Param("menuNumb") String menuNumb, @Param("subxNumb") String subxNumb);

    /** 특정 상위 메뉴 아래의 하위 메뉴 목록을 조회한다. */
    List<MenuVO> getSubMenuList(@Param("menuNumb") String menuNumb);

    /** 신규 상위 메뉴 번호를 조회한다. */
    String getMenuNumb();

    /** 특정 상위 메뉴 아래의 신규 하위 메뉴 번호를 조회한다. */
    String getSubxNumb(@Param("menuNumb") String menuNumb);

    /** 메뉴를 등록한다. */
    void setMenu(MenuVO menu);

    /** 메뉴 상세 정보를 수정한다. */
    void uptMenu(MenuVO menu);

    /** 메뉴를 삭제한다. */
    void delMenu(@Param("menuNumb") String menuNumb, @Param("subxNumb") String subxNumb);
}

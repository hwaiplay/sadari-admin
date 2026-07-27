package org.sadari.admin.sadariadmin.usermenu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuVO;

import java.util.List;

@Mapper
public interface UserMenuMapper {

    /** 사용자 메뉴관리 상위 메뉴 목록 조회 */
    List<UserMenuVO> getUserMenuList();

    /** 사용자 메뉴 상세 조회 */
    UserMenuVO getUserMenuDtl(@Param("menuNumb") String menuNumb, @Param("subxNumb") String subxNumb);

    /** 사용자 하위 메뉴 목록 조회 */
    List<UserMenuVO> getUserSubMenuList(@Param("menuNumb") String menuNumb);

    /** 신규 상위 메뉴 번호 조회 */
    String getUserMenuNumb();

    /** 신규 하위 메뉴 번호 조회 */
    String getUserSubxNumb(@Param("menuNumb") String menuNumb);

    /** 사용자 메뉴 등록 */
    void setUserMenu(UserMenuVO menu);

    /** 사용자 메뉴 수정 */
    void uptUserMenu(UserMenuVO menu);

    /** 사용자 메뉴 삭제 */
    void delUserMenu(@Param("menuNumb") String menuNumb, @Param("subxNumb") String subxNumb);
}

package org.sadari.admin.sadariadmin.authgroup.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.authgroup.vo.AuthGroupVO;
import org.sadari.admin.sadariadmin.authgroup.vo.AuthMenuVO;

import java.util.List;

@Mapper
public interface AuthGroupMapper {

    /** 권한그룹 목록 조회 */
    List<AuthGroupVO> getAuthGroupList();

    /** 권한그룹 상세 조회 */
    AuthGroupVO getAuthGroup(@Param("authCode") String authCode);

    /** 권한그룹 중복 건수 조회 */
    int getAuthGroupCount(@Param("authCode") String authCode);

    /** 권한그룹별 메뉴 권한 조회 */
    List<AuthMenuVO> getAuthMenuList(@Param("authCode") String authCode);

    /** 권한그룹 등록 */
    void setAuthGroup(AuthGroupVO authGroup);

    /** 권한그룹 수정 */
    void uptAuthGroup(AuthGroupVO authGroup);

    /** 메뉴 권한 등록 */
    void setAuthMenu(AuthMenuVO authMenu);

    /** 메뉴 권한 삭제 */
    void delAuthMenu(@Param("authCode") String authCode);

    /** 권한그룹 삭제 */
    void delAuthGroup(@Param("authCode") String authCode);
}

package org.sadari.admin.sadariadmin.adminauth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthVO;

import java.util.List;

@Mapper
public interface AdminAuthMapper {

    /** 관리자 권한 목록 조회 */
    List<AdminAuthVO> getAdminAuthList();

    /** 관리자 존재 건수 조회 */
    int getAdminCount(@Param("admnNumb") Long admnNumb);

    /** 사용 가능한 권한그룹 건수 조회 */
    int getAuthGroupCount(@Param("authCode") String authCode);

    /** 관리자 권한 수정 */
    void uptAdminAuth(AdminAuthVO adminAuth);
}

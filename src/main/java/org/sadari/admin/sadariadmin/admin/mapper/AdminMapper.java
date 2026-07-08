package org.sadari.admin.sadariadmin.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.admin.vo.AdminVO;

@Mapper
public interface AdminMapper {

    /**
     * 관리자 아이디로 관리자 상세 정보를 조회한다.
     */
    AdminVO getAdminDtl(@Param("admnIdxx") String admnIdxx);

    /**
     * 로그인 성공 시 실패 횟수와 마지막 로그인 일자를 수정한다.
     */
    void uptAdminLoginSuccess(@Param("admnNumb") Long admnNumb);

    /**
     * 로그인 실패 시 실패 횟수를 증가시킨다.
     */
    void uptAdminLoginFail(@Param("admnNumb") Long admnNumb);
}

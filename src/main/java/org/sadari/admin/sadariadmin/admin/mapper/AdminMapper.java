package org.sadari.admin.sadariadmin.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.admin.vo.AdminVO;

@Mapper
public interface AdminMapper {

    /**
     * 관리자 상세 조회
     * @Author SeungHyeon.Kang
     * @param admnIdxx
     * @return
     */
    AdminVO getAdminDtl(@Param("admnIdxx") String admnIdxx);

    /**
     * 관리자 로그인 성공 처리
     * @Author SeungHyeon.Kang
     * @param admnNumb
     * @return
     */
    void uptAdminLoginSuccess(@Param("admnNumb") Long admnNumb);

    /**
     * 관리자 로그인 실패 처리
     * @Author SeungHyeon.Kang
     * @param admnNumb
     * @return
     */
    void uptAdminLoginFail(@Param("admnNumb") Long admnNumb);
}

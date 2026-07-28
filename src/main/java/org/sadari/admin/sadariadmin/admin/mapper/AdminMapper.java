package org.sadari.admin.sadariadmin.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.admin.vo.AdminVO;

/**
 * fileName       : AdminMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : AdminMapper role
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 */@Mapper
public interface AdminMapper {

    /**
     * 관리자 상세 조회
     * @author SeungHyeon.Kang
     * @param admnIdxx
     * @return
     */
    AdminVO getAdminDtl(@Param("admnIdxx") String admnIdxx);

    /**
     * 관리자 로그인 성공 처리
     * @author SeungHyeon.Kang
     * @param admnNumb
     * @return
     */
    void uptAdminLoginSuccess(@Param("admnNumb") Long admnNumb);

    /**
     * 관리자 로그인 실패 처리
     * @author SeungHyeon.Kang
     * @param admnNumb
     * @return
     */
    void uptAdminLoginFail(@Param("admnNumb") Long admnNumb);
}

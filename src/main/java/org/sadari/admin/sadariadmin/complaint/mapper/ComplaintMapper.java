package org.sadari.admin.sadariadmin.complaint.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintSearchVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintUpdateVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintVO;

import java.util.List;

/**
 * fileName       : ComplaintMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 관리자 신고 목록과 상세 및 처리 SQL을 연결한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface ComplaintMapper {

    /**
     * 검색 조건에 맞는 신고 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 신고 검색 조건과 페이지 범위
     * @return 현재 페이지 신고 목록
     */
    List<ComplaintVO> getComplaintList(ComplaintSearchVO search);

    /**
     * 검색 조건에 맞는 신고 전체 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 신고 검색 조건
     * @return 검색된 신고 전체 건수
     */
    int getComplaintListCount(ComplaintSearchVO search);

    /**
     * 신고번호에 해당하는 신고 상세를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @return 신고 상세
     */
    ComplaintVO getComplaintDtl(@Param("cmplNumb") Long cmplNumb);

    /**
     * 신고 처리 전 현재 행을 잠금 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @return 잠긴 신고 상세
     */
    ComplaintVO getComplaintForUpdate(@Param("cmplNumb") Long cmplNumb);

    /**
     * 동일 대상의 최근 다른 신고를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param cmplNumb 현재 신고 번호
     * @return 동일 대상의 최근 다른 신고 목록
     */
    List<ComplaintVO> getRelatedComplaintList(@Param("tagtType") String tagtType
                                             , @Param("tagtNumb") Long tagtNumb
                                             , @Param("cmplNumb") Long cmplNumb);

    /**
     * 동일 대상의 다른 신고 전체 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param cmplNumb 현재 신고 번호
     * @return 동일 대상의 다른 신고 건수
     */
    int getRelatedComplaintListCount(@Param("tagtType") String tagtType
                                    , @Param("tagtNumb") Long tagtNumb
                                    , @Param("cmplNumb") Long cmplNumb);

    /**
     * 신고 담당자와 처리 상태 및 처리 내용을 수정한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param update 변경할 신고 처리 정보
     * @param procAdmn 처리 관리자 번호
     * @return 수정 건수
     */
    int uptComplaint(@Param("cmplNumb") Long cmplNumb
                    , @Param("update") ComplaintUpdateVO update
                    , @Param("procAdmn") Long procAdmn);
}

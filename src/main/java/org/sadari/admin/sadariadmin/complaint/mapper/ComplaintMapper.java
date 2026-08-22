package org.sadari.admin.sadariadmin.complaint.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintSearchVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintActionVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintEvidenceVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintTargetFileVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintTargetContentVO;
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
 * 2026-08-22        SeungHyeon.Kang    자동·수동 조치와 증거 원본 조회
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
     * @param tagtHash 신고 대상 버전 SHA-256 해시
     * @param cmplNumb 현재 신고 번호
     * @return 동일 대상의 최근 다른 신고 목록
     */
    List<ComplaintVO> getRelatedComplaintList(@Param("tagtType") String tagtType
                                             , @Param("tagtNumb") Long tagtNumb
                                             , @Param("tagtHash") String tagtHash
                                             , @Param("cmplNumb") Long cmplNumb);

    /**
     * 동일 대상의 다른 신고 전체 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param tagtHash 신고 대상 버전 SHA-256 해시
     * @param cmplNumb 현재 신고 번호
     * @return 동일 대상의 다른 신고 건수
     */
    int getRelatedComplaintCnt(@Param("tagtType") String tagtType
                                    , @Param("tagtNumb") Long tagtNumb
                                    , @Param("tagtHash") String tagtHash
                                    , @Param("cmplNumb") Long cmplNumb);

    /**
     * 동일 대상의 반려를 제외한 유효 신고 누적 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param tagtHash 신고 대상 버전 SHA-256 해시
     * @return 자동 조치 판단에 포함되는 신고 건수
     */
    int getAutoActionCmplCnt(@Param("tagtType") String tagtType
                             , @Param("tagtNumb") Long tagtNumb
                             , @Param("tagtHash") String tagtHash);

    /**
     * 동일 대상에 실제 실행된 자동 조치 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param tagtHash 신고 대상 버전 SHA-256 해시
     * @return 최신 자동 조치 순서의 실행 이력
     */
    List<ComplaintActionVO> getAutoActionList(@Param("tagtType") String tagtType
                                               , @Param("tagtNumb") Long tagtNumb
                                               , @Param("tagtHash") String tagtHash);

    /**
     * 신고번호에 연결된 관리자 전용 프로필 사진 증거 원본을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @return 이미지 증거 원본 또는 만료·미존재 시 null
     */
    ComplaintEvidenceVO getComplaintEvidence(@Param("cmplNumb") Long cmplNumb);

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

    /**
     * 관리자 수동 원본 조치로 해결된 동일 대상의 미처리 신고를 일괄 종결한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 종결할 신고 대상 유형
     * @param tagtNumb 종결할 신고 대상 번호
     * @param procCntn 관리자 수동 조치 결과 내용
     * @param procAdmn 수동 조치 관리자 번호
     * @return 종결된 미처리 신고 건수
     */
    int uptManualComplaints(@Param("tagtType") String tagtType, @Param("tagtNumb") Long tagtNumb
                           , @Param("procCntn") String procCntn, @Param("procAdmn") Long procAdmn);

    /**
     * 신고 당시 버전과 비교할 현재 자동 조치 대상 원문 또는 파일 정보를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param userNumb 신고 대상 소유 사용자 번호
     * @return 현재 서비스에 노출되어 자동 조치 가능한 대상 정보
     */
    ComplaintTargetContentVO getAutoActionTargetDtl(@Param("tagtType") String tagtType
                                                    , @Param("tagtNumb") Long tagtNumb
                                                    , @Param("userNumb") Long userNumb);

    /**
     * 피신고자의 프로필 이미지 파일 메타정보를 잠금 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 피신고자 회원번호
     * @return 프로필 이미지 파일 메타정보
     */
    ComplaintTargetFileVO getTagtProfFileForUpdate(@Param("userNumb") Long userNumb);

    /**
     * 피신고자의 배경 이미지 파일 메타정보를 잠금 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 피신고자 회원번호
     * @return 배경 이미지 파일 메타정보
     */
    ComplaintTargetFileVO getTagtBgimFileForUpdate(@Param("userNumb") Long userNumb);

    /**
     * 피신고자의 프로필 이미지 참조를 제거한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 피신고자 회원번호
     * @param fileNumb 현재 프로필 파일번호
     * @return 수정 건수
     */
    int delTargetProfileImage(@Param("userNumb") Long userNumb, @Param("fileNumb") Long fileNumb);

    /**
     * 피신고자의 배경 이미지 참조를 제거한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 피신고자 회원번호
     * @param fileNumb 현재 배경 파일번호
     * @return 수정 건수
     */
    int delTargetBackgroundImage(@Param("userNumb") Long userNumb, @Param("fileNumb") Long fileNumb);

    /**
     * 다른 사용자 이미지에서 참조하지 않는 파일 메타정보를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param fileNumb 삭제할 파일번호
     * @return 삭제 건수
     */
    int delTagtFileIfUnref(@Param("fileNumb") Long fileNumb);

    /**
     * 피신고자의 자기소개를 NULL 처리한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 피신고자 회원번호
     * @return 수정 건수
     */
    int delTargetUserIntroduction(@Param("userNumb") Long userNumb);

    /**
     * 신고 대상 독후감에 연결된 댓글과 답글 좋아요를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param reptNumb 독후감 번호
     * @param userNumb 독후감 작성자 회원번호
     * @return 삭제 건수
     */
    int delTargetReportReplyLikes(@Param("reptNumb") Long reptNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 대상 독후감에 연결된 대댓글을 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param reptNumb 독후감 번호
     * @param userNumb 독후감 작성자 회원번호
     * @return 삭제 건수
     */
    int delTagtReportChildReply(@Param("reptNumb") Long reptNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 대상 독후감에 연결된 댓글을 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param reptNumb 독후감 번호
     * @param userNumb 독후감 작성자 회원번호
     * @return 삭제 건수
     */
    int delTargetReportReplies(@Param("reptNumb") Long reptNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 대상 독후감 좋아요를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param reptNumb 독후감 번호
     * @param userNumb 독후감 작성자 회원번호
     * @return 삭제 건수
     */
    int delTargetReportLikes(@Param("reptNumb") Long reptNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 대상 독후감을 완전 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param reptNumb 독후감 번호
     * @param userNumb 독후감 작성자 회원번호
     * @return 삭제 건수
     */
    int delTargetReport(@Param("reptNumb") Long reptNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 대상 댓글 또는 답글을 삭제 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param replNumb 댓글 번호
     * @param userNumb 댓글 작성자 회원번호
     * @return 수정 건수
     */
    int delTargetReply(@Param("replNumb") Long replNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 대상 모임 소개를 NULL 처리한다
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @return 수정 건수
     */
    int delTargetClubIntroduction(@Param("clubNumb") Long clubNumb);

    /**
     * 신고 대상 콘텐츠 원본이 현재 조치 가능한 상태로 존재하는지 조회한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param userNumb 신고 대상 작성자 회원번호
     * @return 조치 가능한 원본 건수
     */
    int getTargetContentCount(@Param("tagtType") String tagtType, @Param("tagtNumb") Long tagtNumb
                              , @Param("userNumb") Long userNumb);
}

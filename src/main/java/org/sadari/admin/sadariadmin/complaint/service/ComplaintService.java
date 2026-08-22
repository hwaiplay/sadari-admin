package org.sadari.admin.sadariadmin.complaint.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintDetailVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintEvidenceVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintSearchVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintUpdateVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;

/**
 * fileName       : ComplaintService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 관리자 신고 조회와 처리 및 사용자 신고 대상 이용정지 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 */
public interface ComplaintService {

    /**
     * 관리자 검색 조건에 맞는 신고 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 신고 검색 조건
     * @param admin 로그인 관리자
     * @return 검색된 신고 페이지
     */
    PageData<ComplaintVO> getComplaintList(ComplaintSearchVO search, AdminSessionVO admin);

    /**
     * 신고번호로 신고와 동일 대상 신고 및 사용자 신고 대상을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 로그인 관리자
     * @return 신고 상세
     */
    ComplaintDetailVO getComplaintDtl(Long cmplNumb, AdminSessionVO admin);

    /**
     * 신고번호에 연결된 프로필 사진 신고 증거 원본을 관리자에게 제공한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 로그인 관리자
     * @return 관리자 전용 이미지 증거 원본
     */
    ComplaintEvidenceVO getComplaintEvidence(Long cmplNumb, AdminSessionVO admin);

    /**
     * 신고의 검토 시작 또는 최종 처리 상태를 저장한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param update 변경할 신고 처리 정보
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    ComplaintDetailVO uptComplaint(Long cmplNumb, ComplaintUpdateVO update, AdminSessionVO admin);

    /**
     * 피신고자의 프로필 이미지를 기본 이미지 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    ComplaintDetailVO delTargetProfImage(Long cmplNumb, AdminSessionVO admin);

    /**
     * 피신고자의 배경 이미지를 기본 이미지 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    ComplaintDetailVO delTargetBgimImage(Long cmplNumb, AdminSessionVO admin);

    /**
     * 피신고자의 자기소개를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    ComplaintDetailVO delTargetIntroduction(Long cmplNumb, AdminSessionVO admin);

    /**
     * 신고 유형에 맞는 독후감, 댓글 또는 모임 소개를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    ComplaintDetailVO delTargetContent(Long cmplNumb, AdminSessionVO admin);

    /**
     * 사용자 신고 대상의 관리자 이용정지 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param pageNumber 페이지 번호
     * @param admin 로그인 관리자
     * @return 이용정지 이력 페이지
     */
    PageData<CurrentUserSuspensionVO> getTargetUserSuspList(Long cmplNumb, int pageNumber
                                                               , AdminSessionVO admin);

    /**
     * 사용자 신고 대상에게 기간 또는 무기한 이용정지를 적용한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param suspension 정지 등록값
     * @param admin 처리 관리자
     * @return 등록된 이용정지 이력
     */
    CurrentUserSuspensionVO setTargetUserSuspension(Long cmplNumb, CurrentUserSuspensionVO suspension
                                                   , AdminSessionVO admin);

    /**
     * 사용자 신고 대상의 적용 중인 이용정지를 관리자 해제한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param spndNumb 정지 이력 번호
     * @param request 해제 메모
     * @param admin 처리 관리자
     */
    void uptTargetSuspReleased(Long cmplNumb, Long spndNumb, CurrentUserSuspensionVO request
                                        , AdminSessionVO admin);
}

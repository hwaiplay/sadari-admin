package org.sadari.admin.sadariadmin.currentuser.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserComplaintVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserLoginHistoryVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSearchVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserWithdrawalHistoryVO;

/**
 * fileName       : CurrentUserService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 현재 사용자 조회와 검색 및 계정 이력 확인 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-08-13        SeungHyeon.Kang    삭제 회원의 유효 제재 목록과 해제 업무 추가
 * 2026-08-22        SeungHyeon.Kang    현재 사용자의 받은 신고 이력 업무 추가
 * 2026-08-22        SeungHyeon.Kang    현재 사용자 프로필 정보 삭제 업무 추가
 */
public interface CurrentUserService {

    /**
     * 관리자 검색 조건에 맞는 현재 사용자 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 사용자 검색 조건
     * @param admin 로그인한 관리자
     * @return 검색된 현재 사용자 페이지
     */
    PageData<CurrentUserVO> getCurrentUserList(CurrentUserSearchVO search, AdminSessionVO admin);

    /**
     * 회원번호로 현재 사용자 상세와 활동 요약을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @param admin 로그인한 관리자
     * @return 현재 사용자 상세정보
     */
    CurrentUserVO getCurrentUserDtl(Long userNumb, AdminSessionVO admin);

    /**
     * 현재 사용자의 프로필 사진을 기본 이미지 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 회원번호
     * @param admin 처리 관리자
     * @return 변경된 현재 사용자 상세정보
     */
    CurrentUserVO delUserProfImage(Long userNumb, AdminSessionVO admin);

    /**
     * 현재 사용자의 배경화면을 기본 이미지 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 회원번호
     * @param admin 처리 관리자
     * @return 변경된 현재 사용자 상세정보
     */
    CurrentUserVO delUserBgimImage(Long userNumb, AdminSessionVO admin);

    /**
     * 현재 사용자의 한줄 소개를 NULL 처리한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 회원번호
     * @param admin 처리 관리자
     * @return 변경된 현재 사용자 상세정보
     */
    CurrentUserVO delUserIntroduction(Long userNumb, AdminSessionVO admin);

    /**
     * 현재 사용자의 마스킹된 로그인 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @param pageNumber 조회할 페이지 번호
     * @param admin 로그인한 관리자
     * @return 로그인 이력 페이지
     */
    PageData<CurrentUserLoginHistoryVO> getLoginHistoryList(Long userNumb, int pageNumber, AdminSessionVO admin);

    /**
     * 현재 사용자의 계정 비활성화와 영구 탈퇴 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @param pageNumber 조회할 페이지 번호
     * @param admin 로그인한 관리자
     * @return 계정 처리 이력 페이지
     */
    PageData<CurrentUserWithdrawalHistoryVO> getWithdrawalHistoryList(Long userNumb, int pageNumber
                                                                    , AdminSessionVO admin);

    /**
     * 현재 사용자와 사용자 작성 대상이 받은 신고 누적 건수와 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @param pageNumber 조회할 페이지 번호
     * @param admin 로그인한 관리자
     * @return 받은 신고 이력 페이지
     */
    PageData<CurrentUserComplaintVO> getComplaintHistoryList(Long userNumb, int pageNumber
                                                           , AdminSessionVO admin);

    /**
     * 현재 사용자의 관리자 이용 정지 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param pageNumber 페이지 번호
     * @param admin 로그인 관리자
     * @return 이용 정지 이력 페이지
     */
    PageData<CurrentUserSuspensionVO> getSuspensionHistoryList(Long userNumb, int pageNumber
                                                             , AdminSessionVO admin);

    /**
     * 회원에게 기간 또는 무기한 이용 정지를 적용한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param suspension 정지 유형과 사유 및 기간
     * @param admin 처리 관리자
     * @return 등록된 정지 이력
     */
    CurrentUserSuspensionVO setUserSuspension(Long userNumb, CurrentUserSuspensionVO suspension
                                            , AdminSessionVO admin);

    /**
     * 적용 중인 회원 이용 정지를 관리자 해제한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param spndNumb 정지 이력 번호
     * @param request 해제 메모
     * @param admin 처리 관리자
     */
    void uptUserSuspensionReleased(Long userNumb, Long spndNumb, CurrentUserSuspensionVO request
                                  , AdminSessionVO admin);

    /**
     * 물리 삭제된 회원에게 남아 있는 유효 제재 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 검색할 과거 회원 번호
     * @param pageNumber 조회할 페이지 번호
     * @param admin 로그인 관리자
     * @return 삭제 회원의 유효 제재 페이지
     */
    PageData<CurrentUserSuspensionVO> getDeletedSuspensionList(Long userNumb, int pageNumber
                                                             , AdminSessionVO admin);

    /**
     * 물리 삭제된 회원에게 남아 있는 유효 제재를 해제한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 과거 회원 번호
     * @param spndNumb 제재 이력 번호
     * @param request 필수 해제 메모
     * @param admin 처리 관리자
     */
    void uptDeletedSuspReleased(Long userNumb, Long spndNumb, CurrentUserSuspensionVO request
                               , AdminSessionVO admin);
}

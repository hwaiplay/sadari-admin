package org.sadari.admin.sadariadmin.currentuser.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserComplaintVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserFileVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserLoginHistoryVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSearchVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserStatusEventVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserWithdrawalHistoryVO;

import java.util.List;

/**
 * fileName       : CurrentUserMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 현재 사용자 목록과 상세 및 계정 이력 조회 SQL을 연결한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    회원 상태 변경 Outbox 이벤트 등록 추가
 * 2026-07-30        SeungHyeon.Kang    정지 이력 동기화 상태 수정 추가
 * 2026-08-13        SeungHyeon.Kang    삭제 회원의 유효 제재 조회와 잠금 조회 추가
 * 2026-08-22        SeungHyeon.Kang    신고 이력과 프로필 수동 조치 SQL
 */
@Mapper
public interface CurrentUserMapper {

    /**
     * 검색 조건에 맞는 현재 사용자 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 검색 조건과 페이지 범위
     * @return 현재 페이지 사용자 목록
     */
    List<CurrentUserVO> getCurrentUserList(CurrentUserSearchVO search);

    /**
     * 검색 조건에 맞는 현재 사용자 전체 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 사용자 검색 조건
     * @return 검색된 사용자 전체 건수
     */
    int getCurrentUserListCount(CurrentUserSearchVO search);

    /**
     * 회원번호로 현재 사용자 상세와 활동 요약을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @return 현재 사용자 상세와 활동 요약
     */
    CurrentUserVO getCurrentUserDtl(@Param("userNumb") Long userNumb);

    /**
     * 현재 사용자의 프로필 이미지 파일 메타정보를 잠금 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 사용자 번호
     * @return 프로필 이미지 파일 메타정보
     */
    CurrentUserFileVO getUserProfFileForUpdate(@Param("userNumb") Long userNumb);

    /**
     * 현재 사용자의 배경 이미지 파일 메타정보를 잠금 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 사용자 번호
     * @return 배경 이미지 파일 메타정보
     */
    CurrentUserFileVO getUserBgimFileForUpdate(@Param("userNumb") Long userNumb);

    /**
     * 현재 사용자의 프로필 이미지 참조를 제거한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 사용자 번호
     * @param fileNumb 현재 프로필 파일번호
     * @return 수정 건수
     */
    int delUserProfileImage(@Param("userNumb") Long userNumb, @Param("fileNumb") Long fileNumb);

    /**
     * 현재 사용자의 배경 이미지 참조를 제거한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 사용자 번호
     * @param fileNumb 현재 배경 파일번호
     * @return 수정 건수
     */
    int delUserBackgroundImage(@Param("userNumb") Long userNumb, @Param("fileNumb") Long fileNumb);

    /**
     * 다른 사용자 이미지에서 참조하지 않는 파일 메타정보를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param fileNumb 삭제할 파일번호
     * @return 삭제 건수
     */
    int delUserFileIfUnref(@Param("fileNumb") Long fileNumb);

    /**
     * 현재 사용자의 한줄 소개를 NULL 처리한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 사용자 번호
     * @return 수정 건수
     */
    int delUserIntroduction(@Param("userNumb") Long userNumb);

    /**
     * 현 사용자 프로필 조치로 해결된 미처리 신고를 일괄 종결한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 종결할 사용자 프로필 신고 대상 유형
     * @param userNumb 신고 대상 사용자 번호
     * @param procCntn 관리자 수동 조치 내용
     * @param procAdmn 처리 관리자 번호
     * @return 종결된 미처리 신고 건수
     */
    int uptUserComplaints(@Param("tagtType") String tagtType, @Param("userNumb") Long userNumb
                          , @Param("procCntn") String procCntn, @Param("procAdmn") Long procAdmn);

    /**
     * 회원번호에 해당하는 현재 사용자 존재 여부를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 회원번호
     * @return 현재 사용자 일치 건수
     */
    int getCurrentUserCount(@Param("userNumb") Long userNumb);

    /**
     * 현재 사용자의 로그인 이력을 최신순으로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @param startRow 페이지 시작 행 번호
     * @param endRow 페이지 종료 행 번호
     * @return 마스킹된 로그인 이력 목록
     */
    List<CurrentUserLoginHistoryVO> getLoginHistoryList(@Param("userNumb") Long userNumb
                                                      , @Param("startRow") int startRow
                                                      , @Param("endRow") int endRow);

    /**
     * 현재 사용자의 로그인 이력 전체 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @return 로그인 이력 전체 건수
     */
    int getLoginHistoryListCount(@Param("userNumb") Long userNumb);

    /**
     * 현재 사용자의 계정 처리 이력을 최신순으로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @param startRow 페이지 시작 행 번호
     * @param endRow 페이지 종료 행 번호
     * @return 비활성화와 영구 탈퇴 이력 목록
     */
    List<CurrentUserWithdrawalHistoryVO> getWithdrawalHistoryList(@Param("userNumb") Long userNumb
                                                                , @Param("startRow") int startRow
                                                                , @Param("endRow") int endRow);

    /**
     * 현재 사용자의 계정 처리 이력 전체 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @return 계정 처리 이력 전체 건수
     */
    int getWithdrawalHistoryCnt(@Param("userNumb") Long userNumb);

    /**
     * 현재 사용자와 사용자 작성 대상이 받은 신고 이력을 최신순으로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고 대상 소유 사용자 번호
     * @param startRow 페이지 시작 행 번호
     * @param endRow 페이지 종료 행 번호
     * @return 받은 신고 이력 목록
     */
    List<CurrentUserComplaintVO> getComplaintHistoryList(@Param("userNumb") Long userNumb
                                                        , @Param("startRow") int startRow
                                                        , @Param("endRow") int endRow);

    /**
     * 현재 사용자와 사용자 작성 대상이 받은 신고 누적 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고 대상 소유 사용자 번호
     * @return 받은 신고 누적 건수
     */
    int getComplaintHistoryCnt(@Param("userNumb") Long userNumb);

    /**
     * 회원 행을 잠그고 현재 상태를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @return 현재 회원 상태
     */
    String getUserStatusForUpdate(@Param("userNumb") Long userNumb);

    /**
     * 회원의 적용 중 정지 이력을 잠금 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @return 적용 중 정지 이력
     */
    CurrentUserSuspensionVO getActiveSuspForUpdate(@Param("userNumb") Long userNumb);

    /**
     * 회원 정지 이력을 등록한다
     *
     * @author SeungHyeon.Kang
     * @param suspension 등록할 정지 정보
     * @return 등록 건수
     */
    int setUserSuspension(CurrentUserSuspensionVO suspension);

    /**
     * 적용 중인 회원 정지를 관리자 해제로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param suspension 해제할 정지 정보
     * @return 수정 건수
     */
    int uptUserSuspensionReleased(CurrentUserSuspensionVO suspension);

    /**
     * 종료 시각이 지난 기간 정지를 만료로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param suspension 만료할 정지 정보
     * @return 수정 건수
     */
    int uptUserSuspensionExpired(CurrentUserSuspensionVO suspension);

    /**
     * 회원 상태를 정지로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @return 수정 건수
     */
    int uptCurrentUserSuspended(@Param("userNumb") Long userNumb);

    /**
     * 현재 정지 상태인 회원만 정지 직전 상태로 복구한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param userStat 복구할 회원 상태
     * @return 수정 건수
     */
    int uptUserStatusAfterSuspend(@Param("userNumb") Long userNumb
                                          , @Param("userStat") String userStat);

    /**
     * 회원 상태가 변경된 정지 이력을 사용자 서버 반영 대기 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param spndNumb 반영을 기다릴 정지 이력 번호
     * @return 반영 대기 상태로 변경된 이력 건수
     */
    int uptUserSuspSyncPending(@Param("spndNumb") Long spndNumb);

    /**
     * 사용자 서버가 처리할 회원 상태 변경 Outbox 이벤트를 등록한다
     *
     * @author SeungHyeon.Kang
     * @param event 등록할 회원 상태 변경 이벤트
     * @return 등록 건수
     */
    int setCurrentUserStatusEvent(CurrentUserStatusEventVO event);

    /**
     * 회원 정지 이력을 최신순으로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param startRow 페이지 시작 행
     * @param endRow 페이지 종료 행
     * @return 정지 이력 목록
     */
    List<CurrentUserSuspensionVO> getSuspensionHistoryList(@Param("userNumb") Long userNumb
                                                         , @Param("startRow") int startRow
                                                         , @Param("endRow") int endRow);

    /**
     * 회원 정지 이력 전체 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @return 정지 이력 건수
     */
    int getSuspensionHistoryCnt(@Param("userNumb") Long userNumb);

    /**
     * 물리 삭제된 회원에게 남아 있는 유효 제재를 최신순으로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 검색할 과거 회원 번호
     * @param startRow 페이지 시작 행
     * @param endRow 페이지 종료 행
     * @return 삭제 회원의 유효 제재 목록
     */
    List<CurrentUserSuspensionVO> getDeletedSuspensionList(@Param("userNumb") Long userNumb
                                                          , @Param("startRow") int startRow
                                                          , @Param("endRow") int endRow);

    /**
     * 물리 삭제된 회원에게 남아 있는 유효 제재 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 검색할 과거 회원 번호
     * @return 삭제 회원의 유효 제재 건수
     */
    int getDeletedSuspensionCnt(@Param("userNumb") Long userNumb);

    /**
     * 물리 삭제된 회원의 유효 제재 한 건을 관리자 해제 전에 잠금 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 과거 회원 번호
     * @param spndNumb 제재 이력 번호
     * @return 잠근 유효 제재 이력
     */
    CurrentUserSuspensionVO getDeletedActiveSuspForUpdate(@Param("userNumb") Long userNumb
                                                         , @Param("spndNumb") Long spndNumb);
}

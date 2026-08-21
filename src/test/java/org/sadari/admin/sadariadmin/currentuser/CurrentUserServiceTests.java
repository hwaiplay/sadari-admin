package org.sadari.admin.sadariadmin.currentuser;

import org.junit.jupiter.api.Test;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.currentuser.service.CurrentUserService;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserComplaintVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserLoginHistoryVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSearchVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserWithdrawalHistoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * fileName       : CurrentUserServiceTests
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 실제 MySQL 스키마에서 현재 사용자 목록·상세·이력 조회를 확인한다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    회원 상태 Outbox 통합 저장 구조 반영
 * 2026-07-30        SeungHyeon.Kang    사용자 서버 상태 반영 결과 조회 검증
 * 2026-07-30        SeungHyeon.Kang    로그인 제공자 공통코드명 조회 검증
 * 2026-08-13        SeungHyeon.Kang    물리 삭제 회원의 유효 제재 조회와 해제 검증
 * 2026-08-22        SeungHyeon.Kang    현재 사용자가 받은 신고 누적 건수와 이력 조회 검증
 */
@SpringBootTest
@ActiveProfiles("loc")
@Transactional
class CurrentUserServiceTests {

    // 실제 현재 사용자 Mapper가 연결된 조회 서비스
    @Autowired
    private CurrentUserService currentUserService;
    // 물리 삭제 회원 제재 시나리오를 트랜잭션 안에서 구성할 DB 접근 객체
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 활성 사용자 검색 결과가 상세와 세 이력 조회로 이어지는지 확인한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getUserWithHistories() {
        // 관리자 목록의 기본 검색 조건과 같은 활성 상태를 설정한다.
        CurrentUserSearchVO search = new CurrentUserSearchVO();
        search.setUserStat("ACTIVE");
        // 실제 DB에서 활성 사용자 첫 페이지를 조회한다.
        PageData<CurrentUserVO> userPage = currentUserService.getCurrentUserList(search, createAdminSession());
        // 기본 운영 데이터에 활성 사용자가 존재하는지 확인한다.
        assertFalse(userPage.getItems().isEmpty());
        // 목록의 로그인 제공자가 공통코드 표시명으로 함께 조회되는지 확인한다.
        assertNotNull(userPage.getItems().get(0).getUserProvName());

        // 첫 사용자의 회원번호로 상세 활동 집계를 조회한다.
        CurrentUserVO currentUser = currentUserService.getCurrentUserDtl(
            userPage.getItems().get(0).getUserNumb()
            , createAdminSession()
        );
        // 목록과 상세가 동일한 회원을 가리키는지 확인한다.
        assertEquals(userPage.getItems().get(0).getUserNumb(), currentUser.getUserNumb());
        // 암호화 외부 식별값 없이도 상세 식별에 필요한 닉네임이 조회되는지 확인한다.
        assertNotNull(currentUser.getUserNick());
        // 상세에서도 목록과 동일한 로그인 제공자 공통코드명이 조회되는지 확인한다.
        assertEquals(userPage.getItems().get(0).getUserProvName(), currentUser.getUserProvName());
        // Outbox 처리 여부가 관리자 상세 응답에 항상 포함되는지 확인한다.
        assertNotNull(currentUser.getUserStatusSyncStat());

        // 조회된 로그인 제공자 코드를 셀렉트박스 검색 조건과 같은 방식으로 설정한다.
        CurrentUserSearchVO providerSearch = new CurrentUserSearchVO();
        providerSearch.setUserProv(currentUser.getUserProv());
        // 공통코드에 등록된 제공자로 검색했을 때 현재 사용자가 결과에 포함되는지 확인한다.
        PageData<CurrentUserVO> providerPage = currentUserService.getCurrentUserList(
            providerSearch, createAdminSession()
        );
        assertFalse(providerPage.getItems().isEmpty());

        // 마스킹 로그인 이력 첫 페이지 쿼리를 실행한다.
        PageData<CurrentUserLoginHistoryVO> loginPage = currentUserService.getLoginHistoryList(
            currentUser.getUserNumb()
            , 1
            , createAdminSession()
        );
        // 이력 유무와 무관하게 페이지 응답이 생성되는지 확인한다.
        assertNotNull(loginPage.getItems());

        // 개인정보 자유 입력값을 제외한 계정 처리 이력 첫 페이지 쿼리를 실행한다.
        PageData<CurrentUserWithdrawalHistoryVO> withdrawalPage =
            currentUserService.getWithdrawalHistoryList(currentUser.getUserNumb(), 1, createAdminSession());
        // 이력 유무와 무관하게 페이지 응답이 생성되는지 확인한다.
        assertNotNull(withdrawalPage.getItems());

        // 사용자 본인과 사용자 작성 대상이 받은 신고 이력 첫 페이지 쿼리를 실행한다.
        PageData<CurrentUserComplaintVO> complaintPage =
            currentUserService.getComplaintHistoryList(currentUser.getUserNumb(), 1, createAdminSession());
        // 이력 유무와 관계없이 누적 건수와 신고 이력 페이지가 생성되는지 확인한다.
        assertNotNull(complaintPage.getItems());

        // 관리자 이용 정지 이력 첫 페이지 쿼리를 실행한다
        PageData<CurrentUserSuspensionVO> suspensionPage =
            currentUserService.getSuspensionHistoryList(currentUser.getUserNumb(), 1, createAdminSession());
        // 이력 유무와 관계없이 정지 이력 페이지가 생성되는지 확인한다
        assertNotNull(suspensionPage.getItems());
    }

    /**
     * 사용자가 받은 신고의 누적 건수와 접수 시점 내용을 함께 조회하는지 확인한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getReceivedComplaintHistory() {
        // 신고 대상 소유자 외래키를 만족하는 활성 사용자를 준비한다.
        CurrentUserVO currentUser = getActiveCurrentUser();
        // 테스트 전 해당 사용자가 받은 신고 누적 건수를 DB에서 조회한다.
        int previousCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM TH_CMPLNT WHERE TAGT_USER = ?"
            , Integer.class
            , currentUser.getUserNumb()
        );
        // 대상 원본이 삭제된 뒤에도 확인할 수 있는 접수 시점 스냅샷 신고를 생성한다.
        jdbcTemplate.update(
            """
            INSERT INTO TH_CMPLNT (
                        USER_NUMB
                      , TAGT_TYPE
                      , TAGT_NUMB
                      , TAGT_USER
                      , TAGT_CNTN
                      , CMPL_RSON
                      , CMPL_CNTN
                      , CMPL_STAT
            ) VALUES (  NULL
                      , 'CMPL_USER'
                      , ?
                      , ?
                      , '통합 테스트 대상 내용'
                      , 'CMPL_OTHER'
                      , '통합 테스트 신고 상세'
                      , 'CMPL_RECEIVED'
            )
            """
            , currentUser.getUserNumb(), currentUser.getUserNumb()
        );
        // 같은 트랜잭션에서 대상 소유자 연결이 실제 저장되었는지 먼저 확인한다.
        assertEquals(previousCount + 1, jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM TH_CMPLNT WHERE TAGT_USER = ?"
            , Integer.class
            , currentUser.getUserNumb()
        ));

        // 대상 소유자 기준으로 갱신된 누적 건수와 첫 페이지를 조회한다.
        PageData<CurrentUserComplaintVO> complaintPage = currentUserService.getComplaintHistoryList(
            currentUser.getUserNumb(), 1, createAdminSession()
        );
        // 신규 신고가 누적 횟수에 정확히 반영되는지 확인한다.
        assertEquals(previousCount + 1, complaintPage.getTotalCount());
        // 최신 이력에 접수 당시 대상 내용과 신고 상세가 함께 제공되는지 확인한다.
        assertEquals("통합 테스트 대상 내용", complaintPage.getItems().get(0).getTagtCntn());
        assertEquals("통합 테스트 신고 상세", complaintPage.getItems().get(0).getCmplCntn());
    }

    /**
     * 기간 정지 등록과 즉시 해제가 회원 상태와 이력에 함께 반영되는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setAndReleasePeriodSusp() {
        CurrentUserVO currentUser = getActiveCurrentUser();
        CurrentUserSuspensionVO request = new CurrentUserSuspensionVO();
        request.setSpndType("PERIOD");
        request.setSpndRson("POLICY_VIOLATION");
        request.setSpndCntn("통합 테스트 정지 처리 근거");
        request.setEndxDate(LocalDateTime.now().plusDays(1));

        CurrentUserSuspensionVO created = currentUserService.setUserSuspension(
            currentUser.getUserNumb(), request, createAdminSession()
        );
        assertNotNull(created.getSpndNumb());
        // 정지 등록 트랜잭션에서 생성된 Outbox 이벤트가 관리자에게 반영 대기로 표시되는지 조회한다.
        CurrentUserVO suspendedUser = currentUserService.getCurrentUserDtl(
            currentUser.getUserNumb(), createAdminSession()
        );
        assertEquals("SUSPENDED", suspendedUser.getUserStat());
        assertEquals(Constant.USER_STATUS_SYNC_PENDING, suspendedUser.getUserStatusSyncStat());

        CurrentUserSuspensionVO release = new CurrentUserSuspensionVO();
        release.setRlesCntn("통합 테스트 정지 해제");
        currentUserService.uptUserSuspensionReleased(
            currentUser.getUserNumb(), created.getSpndNumb(), release, createAdminSession()
        );
        // 정지 해제 이벤트도 사용자 서버 처리 전에는 동일하게 반영 대기로 표시되는지 조회한다.
        CurrentUserVO releasedUser = currentUserService.getCurrentUserDtl(
            currentUser.getUserNumb(), createAdminSession()
        );
        assertEquals("ACTIVE", releasedUser.getUserStat());
        assertEquals(Constant.USER_STATUS_SYNC_PENDING, releasedUser.getUserStatusSyncStat());
    }

    /**
     * 일반 관리자가 무기한 정지를 직접 요청해도 서버에서 차단하는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setIndefSuspNeedsSuper() {
        CurrentUserSuspensionVO request = new CurrentUserSuspensionVO();
        request.setSpndType("INDEFINITE");
        request.setSpndRson("SERVICE_ABUSE");

        BusinessException exception = assertThrows(BusinessException.class, () ->
            currentUserService.setUserSuspension(getActiveCurrentUser().getUserNumb(), request, createAdminSession())
        );
        assertEquals(ResultEnum.USER_SUSPENSION_INDEFINITE_FORBIDDEN, exception.getResultEnum());
    }

    /**
     * 물리 삭제된 회원의 유효 제재를 목록에서 조회하고 필수 메모와 함께 해제하는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getAndReleaseDeletedSuspension() {
        // 현재 회원과 제재 이력에서 사용하지 않는 과거 회원 번호를 계산한다
        Long deletedUserNumb = jdbcTemplate.queryForObject(
            "SELECT GREATEST(COALESCE((SELECT MAX(USER_NUMB) FROM TM_USERXM), 0), COALESCE((SELECT MAX(USER_NUMB) FROM TH_USSPND), 0)) + 1"
            , Long.class
        );
        // 테스트 트랜잭션에서 충돌하지 않을 새 제재 이력 번호를 계산한다
        Long spndNumb = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(SPND_NUMB), 0) + 1 FROM TH_USSPND", Long.class
        );
        // 회원 원본 없이 감사 이력만 남은 무기한 제재를 생성한다
        jdbcTemplate.update(
            "INSERT INTO TH_USSPND (SPND_NUMB, USER_NUMB, PREV_STAT, SPND_TYPE, SPND_RSON, SPND_CNTN, SPND_STAT, SYNC_STAT, STRT_DATE, REGI_ADMN, REGI_DATE, UPDT_ADMN, UPDT_DATE) VALUES (?, ?, 'SUSPENDED', 'INDEFINITE', 'SERVICE_ABUSE', '삭제 회원 제재 테스트', 'ACTIVE', 'COMPLETED', CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP)"
            , spndNumb, deletedUserNumb
        );

        // 과거 회원 번호로 삭제 회원의 유효 제재 목록을 조회한다
        PageData<CurrentUserSuspensionVO> suspensionPage = currentUserService.getDeletedSuspensionList(
            deletedUserNumb, 1, createAdminSession()
        );
        // OAuth 식별값 없이 과거 회원 번호 기준 제재 한 건이 조회되는지 검증한다
        assertEquals(1, suspensionPage.getTotalCount());
        // 목록에서 조회한 제재 번호가 보존 이력과 일치하는지 검증한다
        assertEquals(spndNumb, suspensionPage.getItems().get(0).getSpndNumb());

        // 삭제 회원 제재 해제 근거를 기록할 요청을 생성한다
        CurrentUserSuspensionVO release = new CurrentUserSuspensionVO();
        // 감사 이력에 필수로 남길 관리자 판단 근거를 설정한다
        release.setRlesCntn("동일 Kakao 계정 신규 가입 허용");
        // 회원 원본을 복구하지 않고 보존된 제재 이력만 해제한다
        currentUserService.uptDeletedSuspReleased(deletedUserNumb, spndNumb, release, createAdminSession());

        // 해제 뒤 제재 상태가 관리자 해제로 변경됐는지 조회한다
        String suspensionStatus = jdbcTemplate.queryForObject(
            "SELECT SPND_STAT FROM TH_USSPND WHERE SPND_NUMB = ?", String.class, spndNumb
        );
        // 같은 Kakao 계정의 신규 가입을 허용할 해제 상태가 기록되는지 검증한다
        assertEquals("RELEASED", suspensionStatus);
        // 해제 판단 근거가 감사 이력에 원문으로 보존되는지 검증한다
        String releaseContent = jdbcTemplate.queryForObject(
            "SELECT RLES_CNTN FROM TH_USSPND WHERE SPND_NUMB = ?", String.class, spndNumb
        );
        // 관리자 입력 해제 메모가 정확히 저장되는지 검증한다
        assertEquals("동일 Kakao 계정 신규 가입 허용", releaseContent);
    }

    /**
     * 기간 정지 테스트에 사용할 활성 사용자 한 명을 조회한다
     *
     * @author SeungHyeon.Kang
     * @return 활성 사용자
     */
    private CurrentUserVO getActiveCurrentUser() {
        CurrentUserSearchVO search = new CurrentUserSearchVO();
        search.setUserStat("ACTIVE");
        return currentUserService.getCurrentUserList(search, createAdminSession()).getItems().get(0);
    }

    /**
     * 테스트 요청에 사용할 관리자 세션을 생성한다.
     *
     * @author SeungHyeon.Kang
     * @return 조회 권한 확인을 통과할 관리자 세션
     */
    private AdminSessionVO createAdminSession() {
        // 서비스 로그인 검증에 사용할 관리자 세션을 생성한다.
        AdminSessionVO admin = new AdminSessionVO();
        // 실제 관리자 메뉴 권한과 연결된 관리자 번호를 설정한다.
        admin.setAdmnNumb(1L);
        // 일반 관리자는 기간 정지만 등록할 수 있도록 권한 코드를 설정한다
        admin.setAuthCode("ADMIN");
        // 완성된 테스트 관리자 세션을 반환한다.
        return admin;
    }
}

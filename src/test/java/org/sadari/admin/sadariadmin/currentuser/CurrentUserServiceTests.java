package org.sadari.admin.sadariadmin.currentuser;

import org.junit.jupiter.api.Test;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.currentuser.service.CurrentUserService;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserLoginHistoryVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSearchVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserWithdrawalHistoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
 */
@SpringBootTest
@ActiveProfiles("loc")
@Transactional
class CurrentUserServiceTests {

    // 실제 현재 사용자 Mapper가 연결된 조회 서비스
    @Autowired
    private CurrentUserService currentUserService;

    /**
     * 활성 사용자 검색 결과가 상세와 두 이력 조회로 이어지는지 확인한다.
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

        // 관리자 이용 정지 이력 첫 페이지 쿼리를 실행한다
        PageData<CurrentUserSuspensionVO> suspensionPage =
            currentUserService.getSuspensionHistoryList(currentUser.getUserNumb(), 1, createAdminSession());
        // 이력 유무와 관계없이 정지 이력 페이지가 생성되는지 확인한다
        assertNotNull(suspensionPage.getItems());
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

package org.sadari.admin.sadariadmin.readingclub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.readingclub.mapper.ReadingClubMapper;
import org.sadari.admin.sadariadmin.readingclub.service.ReadingClubService;
import org.sadari.admin.sadariadmin.readingclub.service.impl.ReadingClubServiceImpl;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubActionVO;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubVO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * fileName       : ReadingClubServiceTests
 * author         : HanWon.Jang
 * date           : 2026-09-04
 * description    : 관리자 독서 모임 조치의 상태 전이와 감사 저장을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-04        HanWon.Jang        최초 생성
 */
@ExtendWith(MockitoExtension.class)
class ReadingClubServiceTests {

    // 관리자 독서 모임 Mapper 대역
    @Mock
    private ReadingClubMapper readingClubMapper;

    // 테스트할 관리자 독서 모임 서비스
    private ReadingClubService readingClubService;

    /** 각 테스트에서 사용할 관리자 독서 모임 서비스를 생성한다. */
    @BeforeEach
    void setUp() {
        // Mapper 대역으로 관리자 독서 모임 서비스를 생성한다.
        readingClubService = new ReadingClubServiceImpl(readingClubMapper);
    }

    /** 모집 중지가 운영 상태를 유지하고 감사 이력과 알림을 저장하는지 확인한다. */
    @Test
    void setRecruitStop() {
        // 정상 운영 중이며 모집 가능한 모임을 준비한다.
        ReadingClubVO currentClub = createClub("ACTIVE", "Y");
        // 조치 뒤 모집만 중지된 최신 상세를 준비한다.
        ReadingClubVO updatedClub = createClub("ACTIVE", "N");
        // 잠금 조회에서 현재 모임을 반환하도록 설정한다.
        when(readingClubMapper.getClubForUpdate(10L)).thenReturn(currentClub);
        // 모집 중지 갱신이 한 행에 반영되도록 설정한다.
        when(readingClubMapper.uptRecruitStopped(10L)).thenReturn(1);
        // 감사 이력 등록이 성공하도록 설정한다.
        when(readingClubMapper.setClubAction(org.mockito.ArgumentMatchers.any())).thenReturn(1);
        // 조치 뒤 최신 상세를 반환하도록 설정한다.
        when(readingClubMapper.getClubDtl(10L)).thenReturn(updatedClub);
        // 모집 중지 요청을 준비한다.
        ReadingClubActionVO request = createAction("RECRUIT_STOP");

        // 관리자 모집 중지 조치를 실행한다.
        ReadingClubVO result = readingClubService.setClubAction(10L, request, createAdmin());

        // 운영 상태는 유지되고 모집 가능 여부만 중지됐는지 확인한다.
        assertEquals("ACTIVE", result.getClubStat());
        // 등록된 감사 이력의 처리 전후 상태를 확인할 캡처를 준비한다.
        ArgumentCaptor<ReadingClubActionVO> actionCaptor = ArgumentCaptor.forClass(ReadingClubActionVO.class);
        // 감사 이력 등록 요청을 캡처한다.
        verify(readingClubMapper).setClubAction(actionCaptor.capture());
        // 모집 중지는 처리 전후 운영 상태가 동일하게 기록되는지 확인한다.
        assertEquals("ACTIVE", actionCaptor.getValue().getBefrStat());
        // 기존 모임원 활동을 유지하는 처리 후 상태를 확인한다.
        assertEquals("ACTIVE", actionCaptor.getValue().getAftrStat());
        // 영향 회원 안내 알림이 같은 조치 정보로 생성되는지 확인한다.
        verify(readingClubMapper).setActionNotifications(request);
    }

    /** 관리자 이용 정지가 정상 운영 모임을 일시 중지로 전환하는지 확인한다. */
    @Test
    void setClubSuspended() {
        // 정상 운영 중인 모임을 준비한다.
        ReadingClubVO currentClub = createClub("ACTIVE", "Y");
        // 조치 뒤 일시 중지된 최신 상세를 준비한다.
        ReadingClubVO updatedClub = createClub("PAUSED", "Y");
        // 잠금 조회에서 현재 모임을 반환하도록 설정한다.
        when(readingClubMapper.getClubForUpdate(10L)).thenReturn(currentClub);
        // 이용 정지 갱신이 한 행에 반영되도록 설정한다.
        when(readingClubMapper.uptClubSuspended(10L)).thenReturn(1);
        // 감사 이력 등록이 성공하도록 설정한다.
        when(readingClubMapper.setClubAction(org.mockito.ArgumentMatchers.any())).thenReturn(1);
        // 조치 뒤 최신 상세를 반환하도록 설정한다.
        when(readingClubMapper.getClubDtl(10L)).thenReturn(updatedClub);

        // 관리자 이용 정지 조치를 실행한다.
        ReadingClubVO result = readingClubService.setClubAction(
                10L, createAction("SUSPEND"), createAdmin());

        // 이용 정지 뒤 모임이 일시 중지 상태인지 확인한다.
        assertEquals("PAUSED", result.getClubStat());
        // 이용 정지 상태 변경 SQL이 실행됐는지 확인한다.
        verify(readingClubMapper).uptClubSuspended(10L);
    }

    /** 관리자 이력이 없는 정책상 일시 중지 모임은 해제하지 않는지 확인한다. */
    @Test
    void restoreRejectsPolicyPause() {
        // 관리자 조치가 아닌 일시 중지 모임을 준비한다.
        ReadingClubVO currentClub = createClub("PAUSED", "N");
        // 잠금 조회에서 정책상 일시 중지 모임을 반환하도록 설정한다.
        when(readingClubMapper.getClubForUpdate(10L)).thenReturn(currentClub);
        // 관리자 조치 이력이 없는 상태를 반환하도록 설정한다.
        when(readingClubMapper.getLatestAction(10L)).thenReturn(null);

        // 근거가 없는 관리자 해제 요청의 업무 예외를 확인한다.
        BusinessException exception = assertThrows(BusinessException.class, () ->
                readingClubService.setClubAction(10L, createAction("RESTORE"), createAdmin()));

        // 현재 상태에서 해제할 수 없다는 충돌 결과인지 확인한다.
        assertEquals(ResultEnum.READING_CLUB_ACTION_CONFLICT, exception.getResultEnum());
        // 정책상 일시 중지 상태를 정상 운영으로 덮어쓰지 않았는지 확인한다.
        verify(readingClubMapper, never()).uptClubRestored(10L);
        // 실패한 조치에 감사 이력을 만들지 않았는지 확인한다.
        verify(readingClubMapper, never()).setClubAction(org.mockito.ArgumentMatchers.any());
    }

    /** 테스트에 사용할 모임 운영 상태를 생성한다. */
    private ReadingClubVO createClub(String clubStat, String rcrtYsno) {
        // 지정한 상태의 관리자용 모임 정보를 생성한다.
        ReadingClubVO club = new ReadingClubVO();
        // 테스트 대상 모임 번호를 설정한다.
        club.setClubNumb(10L);
        // 테스트할 모임 운영 상태를 설정한다.
        club.setClubStat(clubStat);
        // 테스트할 신규 회원 모집 가능 여부를 설정한다.
        club.setRcrtYsno(rcrtYsno);
        // 완성된 모임 운영 상태를 반환한다.
        return club;
    }

    /** 테스트에 사용할 관리자 조치 요청을 생성한다. */
    private ReadingClubActionVO createAction(String actionType) {
        // 관리자 조치 유형과 근거를 담을 요청을 생성한다.
        ReadingClubActionVO action = new ReadingClubActionVO();
        // 테스트할 관리자 조치 유형을 설정한다.
        action.setActnType(actionType);
        // 필수 감사 근거를 설정한다.
        action.setActnRson("운영 정책 위반 확인");
        // 완성된 관리자 조치 요청을 반환한다.
        return action;
    }

    /** 테스트 요청에 사용할 관리자 세션을 생성한다. */
    private AdminSessionVO createAdmin() {
        // 로그인 검증과 감사 관리자 번호에 사용할 세션을 생성한다.
        AdminSessionVO admin = new AdminSessionVO();
        // 관리자 조치 이력에 남길 관리자 번호를 설정한다.
        admin.setAdmnNumb(1L);
        // 완성된 관리자 세션을 반환한다.
        return admin;
    }
}

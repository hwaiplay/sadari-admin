package org.sadari.admin.sadariadmin.statistics;

import org.junit.jupiter.api.Test;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.statistics.service.UserStatisticsService;
import org.sadari.admin.sadariadmin.statistics.vo.UserStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * fileName       : UserStatisticsServiceTests
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 실제 MySQL 원천 테이블의 사용자 통계 실시간 집계를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 * 2026-08-13        SeungHyeon.Kang    사용자 정착과 이탈 통계 검증 추가
 * 2026-08-13        SeungHyeon.Kang    6개월과 1년 조회 기간 검증 추가
 * 2026-08-13        SeungHyeon.Kang    유지율과 이탈 중심 응답 검증으로 정리
 */
@SpringBootTest
@ActiveProfiles("loc")
class UserStatisticsServiceTests {

    // 실제 사용자 통계 Mapper가 연결된 조회 서비스
    @Autowired
    private UserStatisticsService userStatisticsService;

    /**
     * 30일 대시보드의 네 통계 영역이 원천 테이블 조회 결과로 구성되는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getThirtyDayUserStatistics() {
        // 실제 관리자 세션과 동일한 최소 인증 정보를 생성한다
        AdminSessionVO admin = new AdminSessionVO();
        // 관리자 인증 여부 검증에 사용할 번호를 설정한다
        admin.setAdmnNumb(1L);

        // 오늘을 포함한 30일 사용자 통계를 실시간 조회한다
        UserStatisticsVO statistics = userStatisticsService.getUserStatistics(30, admin);

        // 조회 기간이 오늘을 포함한 30일인지 확인한다
        assertEquals(LocalDate.now().minusDays(29), statistics.getStartDate());
        assertEquals(LocalDate.now(), statistics.getEndDate());
        // 날짜가 없는 날도 포함해 30개의 추세 점이 생성되는지 확인한다
        assertEquals(30, statistics.getTrendList().size());
        // 상태 공통코드와 미접속 집계가 모두 응답에 포함되는지 확인한다
        assertFalse(statistics.getStatusList().isEmpty());
        assertNotNull(statistics.getInactivity());
        // 가입 후 재방문 유지율이 1일과 7일 및 30일 세 구간으로 생성되는지 확인한다
        assertEquals(3, statistics.getRetentionList().size());
        // 날짜가 없는 날도 포함해 30개의 계정 이탈 추세 점이 생성되는지 확인한다
        assertEquals(30, statistics.getChurnTrendList().size());
    }

    /**
     * 90일 조회에서도 날짜 연속성과 세 유지율 구간이 유지되는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getNinetyDayUserStatistics() {
        // 실제 관리자 세션과 동일한 최소 인증 정보를 생성한다
        AdminSessionVO admin = new AdminSessionVO();
        // 관리자 인증 여부 검증에 사용할 번호를 설정한다
        admin.setAdmnNumb(1L);

        // 오늘을 포함한 90일 사용자 통계를 실시간 조회한다
        UserStatisticsVO statistics = userStatisticsService.getUserStatistics(90, admin);

        // 신규 가입과 활동 및 활성 회원 추세가 90개 날짜로 생성되는지 확인한다
        assertEquals(90, statistics.getTrendList().size());
        // 계정 이탈과 복구 추세가 90개 날짜로 생성되는지 확인한다
        assertEquals(90, statistics.getChurnTrendList().size());
        // 긴 조회 기간에서도 세 유지율 구간을 동일하게 제공하는지 확인한다
        assertEquals(3, statistics.getRetentionList().size());
    }

    /**
     * 6개월 조회에서 오늘을 포함한 180일 추세가 생성되는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getSixMonthUserStatistics() {
        // 실제 관리자 세션과 동일한 최소 인증 정보를 생성한다
        AdminSessionVO admin = new AdminSessionVO();
        // 관리자 인증 여부 검증에 사용할 번호를 설정한다
        admin.setAdmnNumb(1L);

        // 오늘을 포함한 180일 사용자 통계를 실시간 조회한다
        UserStatisticsVO statistics = userStatisticsService.getUserStatistics(180, admin);

        // 신규 가입과 활동 및 활성 회원 추세가 180개 날짜로 생성되는지 확인한다
        assertEquals(180, statistics.getTrendList().size());
        // 계정 이탈과 복구 추세가 180개 날짜로 생성되는지 확인한다
        assertEquals(180, statistics.getChurnTrendList().size());
    }

    /**
     * 1년 조회에서 오늘을 포함한 365일 추세가 생성되는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getOneYearUserStatistics() {
        // 실제 관리자 세션과 동일한 최소 인증 정보를 생성한다
        AdminSessionVO admin = new AdminSessionVO();
        // 관리자 인증 여부 검증에 사용할 번호를 설정한다
        admin.setAdmnNumb(1L);

        // 오늘을 포함한 365일 사용자 통계를 실시간 조회한다
        UserStatisticsVO statistics = userStatisticsService.getUserStatistics(365, admin);

        // 조회 기간이 오늘을 포함한 365일인지 확인한다
        assertEquals(LocalDate.now().minusDays(364), statistics.getStartDate());
        assertEquals(LocalDate.now(), statistics.getEndDate());
        // 신규 가입과 활동 및 활성 회원 추세가 365개 날짜로 생성되는지 확인한다
        assertEquals(365, statistics.getTrendList().size());
        // 계정 이탈과 복구 추세가 365개 날짜로 생성되는지 확인한다
        assertEquals(365, statistics.getChurnTrendList().size());
    }
}

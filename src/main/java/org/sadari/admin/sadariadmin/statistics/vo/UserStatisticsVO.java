package org.sadari.admin.sadariadmin.statistics.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * fileName       : UserStatisticsVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 관리자 사용자 현황과 유지 및 이탈 통계를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 * 2026-08-13        SeungHyeon.Kang    유지율과 전환 및 활동 구성과 이탈 추세 추가
 * 2026-08-13        SeungHyeon.Kang    미사용 전환과 활동 깊이 응답 제거
 */
@Data
public class UserStatisticsVO {

    // 통계 조회 시작일
    private LocalDate startDate;

    // 통계 조회 종료일
    private LocalDate endDate;

    // 통계 생성 일시
    private LocalDateTime generatedAt;

    // 상태별 현재 회원 수
    private List<UserStatusStatistics> statusList;

    // 일별 신규 가입과 활동 및 활성 회원 추세
    private List<UserTrendStatistics> trendList;

    // 현재 정상 회원의 미접속 기간별 수
    private UserInactivityStatistics inactivity;

    // 가입 후 기간별 재방문 유지율
    private List<UserRetentionStatistics> retentionList;

    // 일별 계정 비활성화와 영구 탈퇴 및 정지와 복구 수
    private List<UserChurnTrendStatistics> churnTrendList;

    /** 상태별 현재 회원 수 */
    @Data
    public static class UserStatusStatistics {

        // 회원 상태 코드
        private String userStat;

        // 회원 상태명
        private String userStatName;

        // 현재 회원 수
        private long userCntt;
    }

    /** 일별 신규 가입과 활동 및 활성 회원 추세 */
    @Data
    public static class UserTrendStatistics {

        // 통계 기준 일자
        private LocalDate statDate;

        // 신규 가입자 수
        private long joinCntt;

        // 독후감 등록 수
        private long reportCntt;

        // 댓글 등록 수
        private long replyCntt;

        // 좋아요 등록 수
        private long likeCntt;

        // 팔로우 등록 수
        private long followCntt;

        // 일간 활성 회원 수
        private long dauCntt;

        // 최근 7일 활성 회원 수
        private long wauCntt;

        // 최근 30일 활성 회원 수
        private long mauCntt;
    }

    /** 정상 회원의 현재 미접속 기간별 수 */
    @Data
    public static class UserInactivityStatistics {

        // 30일 이상 90일 미만 미접속 정상 회원 수
        private long inactive30Cntt;

        // 90일 이상 미접속 정상 회원 수
        private long inactive90Cntt;
    }

    // 가입 후 기간별 재방문 유지율
    @Data
    public static class UserRetentionStatistics {

        // 재방문 확인 기준 일수
        private int periodDays;

        // 재방문 확인 기간이 지난 가입자 수
        private long cohortCntt;

        // 기준 기간 안에 다시 로그인한 가입자 수
        private long retainedCntt;

        // 재방문 확인 기간이 지난 가입자의 재방문 비율
        private double retentionRate;
    }

    // 일별 계정 이탈과 복구 처리 수
    @Data
    public static class UserChurnTrendStatistics {

        // 계정 처리 기준 일자
        private LocalDate statDate;

        // 계정 비활성화 요청 수
        private long withdrawnCntt;

        // 영구 탈퇴 요청 수
        private long deleteCntt;

        // 관리자 이용 정지 시작 수
        private long suspendedCntt;

        // 영구 탈퇴 취소 또는 계정 복구 수
        private long restoredCntt;
    }
}

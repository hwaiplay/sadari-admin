package org.sadari.admin.sadariadmin.statistics.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * fileName       : UserStatisticsSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 관리자 사용자 통계의 조회 시작일과 종료일을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Data
public class UserStatisticsSearchVO {

    // 통계 조회 시작일
    private LocalDate startDate;

    // 통계 조회 종료일
    private LocalDate endDate;
}

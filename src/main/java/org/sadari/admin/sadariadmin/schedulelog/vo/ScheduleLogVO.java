package org.sadari.admin.sadariadmin.schedulelog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : ScheduleLogVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-28
 * description    : 스케줄러 실행 결과 요약 정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    스케줄러 코드명 추가
 */
@Data
public class ScheduleLogVO {

    // 스케줄러 실행 번호
    private Long runxNumb;

    // 스케줄러 구분 코드
    private String schdCode;

    // 스케줄러 구분 코드명
    private String schdCodeName;

    // 실행된 스케줄러 메서드명
    private String methName;

    // 스케줄러 실행 상태 코드
    private String execStat;

    // 스케줄러 실행 시작 일시
    private LocalDateTime strtDate;

    // 스케줄러 실행 종료 일시
    private LocalDateTime fnshDate;

    // 전체 처리 대상 건수
    private Integer trgtCntt;

    // 성공 처리 건수
    private Integer succCntt;

    // 실패 처리 건수
    private Integer failCntt;

    // 스케줄러 실행 소요 시간 밀리초
    private Long execMsec;
}

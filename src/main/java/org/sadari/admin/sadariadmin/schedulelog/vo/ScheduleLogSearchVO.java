package org.sadari.admin.sadariadmin.schedulelog.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * fileName       : ScheduleLogSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-31
 * description    : 스케줄러 실행 로그 검색 조건과 페이지 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        SeungHyeon.Kang    최초 생성
 */
@Data
public class ScheduleLogSearchVO {

    // 요청 페이지 번호
    private Integer page = 1;

    // 실행번호 또는 메서드명 검색어
    private String keyword;

    // 스케줄러 코드
    private String schdCode;

    // 실행 상태
    private String execStat;

    // 실행 시작일 검색 시작일
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate strtDateFrom;

    // 실행 시작일 검색 종료일
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate strtDateTo;

    // 페이지 조회 시작 행 번호
    private int startRow;

    // 페이지 조회 종료 행 번호
    private int endRow;
}

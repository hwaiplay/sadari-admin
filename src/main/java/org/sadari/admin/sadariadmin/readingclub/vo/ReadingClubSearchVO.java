package org.sadari.admin.sadariadmin.readingclub.vo;

import java.time.LocalDate;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * fileName       : ReadingClubSearchVO
 * author         : HanWon.Jang
 * date           : 2026-09-04
 * description    : 관리자 독서 모임 목록 검색 조건과 페이지 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-04        HanWon.Jang        최초 생성
 */
@Data
public class ReadingClubSearchVO {

    // 요청 페이지 번호
    private Integer page = 1;

    // 모임번호, 모임명 또는 모임장 닉네임 검색어
    private String keyword;

    // 모임 운영 상태
    private String clubStat;

    // 모임 공개 범위
    private String clubVisb;

    // 모임 가입 방식
    private String joinType;

    // 모집 가능 여부
    private String rcrtYsno;

    // 생성일 검색 시작일
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate regiDateFrom;

    // 생성일 검색 종료일
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate regiDateTo;

    // 페이지 조회 시작 행 번호
    private int startRow;

    // 페이지 조회 종료 행 번호
    private int endRow;
}

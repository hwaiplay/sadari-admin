package org.sadari.admin.sadariadmin.complaint.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * fileName       : ComplaintSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 관리자 신고 목록의 검색 조건과 페이지 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintSearchVO {

    // 요청 페이지 번호
    private Integer page = 1;

    // 신고 번호
    private Long cmplNumb;

    // 신고 처리 상태 세부코드
    private String cmplStat;

    // 신고 대상 유형 세부코드
    private String tagtType;

    // 신고 대상 번호
    private Long tagtNumb;

    // 신고 사유 세부코드
    private String cmplRson;

    // 신고자 회원번호 또는 닉네임 검색어
    private String reporterKeyword;

    // 신고 접수일 검색 시작일
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate regiDateFrom;

    // 신고 접수일 검색 종료일
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate regiDateTo;

    // 페이지 조회 시작 행 번호
    private int startRow;

    // 페이지 조회 종료 행 번호
    private int endRow;
}

package org.sadari.admin.sadariadmin.alim.vo;

import lombok.Data;

/**
 * fileName       : AlimTempSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-31
 * description    : 알림 템플릿 목록 검색 조건과 페이지 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        SeungHyeon.Kang    최초 생성
 */
@Data
public class AlimTempSearchVO {

    // 요청 페이지 번호
    private Integer page = 1;

    // 템플릿 코드 또는 관리용 제목 검색어
    private String keyword;

    // 알림 상황 코드
    private String alimSitu;

    // 사용 여부
    private String useeYsno;

    // 페이지 조회 시작 행 번호
    private int startRow;

    // 페이지 조회 종료 행 번호
    private int endRow;
}

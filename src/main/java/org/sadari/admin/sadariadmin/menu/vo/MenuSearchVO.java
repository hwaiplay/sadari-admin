package org.sadari.admin.sadariadmin.menu.vo;

import lombok.Data;

/**
 * fileName       : MenuSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-31
 * description    : 관리자 메뉴 목록 검색 조건과 페이지 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        SeungHyeon.Kang    최초 생성
 */
@Data
public class MenuSearchVO {

    // 요청 페이지 번호
    private Integer page = 1;

    // 메뉴명 또는 URL 검색어
    private String keyword;

    // 사용 여부
    private String useeYsno;

    // 페이지 조회 시작 행 번호
    private int startRow;

    // 페이지 조회 종료 행 번호
    private int endRow;
}

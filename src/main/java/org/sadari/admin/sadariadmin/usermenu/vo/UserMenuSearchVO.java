package org.sadari.admin.sadariadmin.usermenu.vo;

import lombok.Data;

/**
 * fileName       : UserMenuSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-31
 * description    : 사용자 메뉴 목록 검색 조건과 페이지 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        SeungHyeon.Kang    최초 생성
 * 2026-08-10        SeungHyeon.Kang    메뉴 단계 검색 조건 추가
 */
@Data
public class UserMenuSearchVO {

    // 요청 페이지 번호
    private Integer page = 1;

    // 메뉴명 또는 URL 검색어
    private String keyword;

    // 햄버거 메뉴 노출 여부
    private String showYsno;

    // 사용 여부
    private String useeYsno;

    // 메뉴 단계
    private Integer menuLevl;

    // 페이지 조회 시작 행 번호
    private int startRow;

    // 페이지 조회 종료 행 번호
    private int endRow;
}

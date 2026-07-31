package org.sadari.admin.sadariadmin.popup.vo;

import lombok.Data;

/**
 * fileName       : PopupContentSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-31
 * description    : 팝업 콘텐츠 목록 검색 조건과 페이지 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        SeungHyeon.Kang    최초 생성
 */
@Data
public class PopupContentSearchVO {

    // 요청 페이지 번호
    private Integer page = 1;

    // 팝업 코드 또는 관리용 제목 검색어
    private String keyword;

    // 팝업 사용 화면 구분 코드
    private String popuSitu;

    // 페이지 조회 시작 행 번호
    private int startRow;

    // 페이지 조회 종료 행 번호
    private int endRow;
}

package org.sadari.admin.sadariadmin.alimicon.vo;

import lombok.Data;

/**
 * fileName       : AlimIconSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-12
 * description    : 알림 아이콘 목록 검색 조건과 페이지 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-12        SeungHyeon.Kang    최초 생성
 */
@Data
public class AlimIconSearchVO {

    // 요청 페이지 번호
    private Integer page = 1;

    // 아이콘 코드 또는 관리명 검색어
    private String keyword;

    // 현재 선택 가능 여부
    private String useeYsno;

    // 페이지 조회 시작 행 번호
    private int startRow;

    // 페이지 조회 종료 행 번호
    private int endRow;
}

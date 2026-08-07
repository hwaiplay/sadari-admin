package org.sadari.admin.sadariadmin.notice.vo;

import lombok.Data;

/**
 * fileName       : NoticeSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 공지사항 목록 검색어와 페이징 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@Data
public class NoticeSearchVO {

    // 제목 검색어
    private String keyword;
    // 현재 페이지 번호
    private int page = 1;
    // 조회 시작 행 번호
    private int startRow;
    // 조회 종료 행 번호
    private int endRow;
}

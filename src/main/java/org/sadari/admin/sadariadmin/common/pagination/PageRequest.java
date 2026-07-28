package org.sadari.admin.sadariadmin.common.pagination;

import lombok.Getter;

/**
 * fileName       : PageRequest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-28
 * description    : 목록 SQL에 전달할 페이지 범위를 계산한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        SeungHyeon.Kang    최초 생성
 */
@Getter
public class PageRequest {

    // 페이지당 조회 건수
    public static final int PAGE_SIZE = 20;

    // 현재 페이지 번호
    private final int pageNumber;

    // 조회 시작 행 번호
    private final int startRow;

    // 조회 종료 행 번호
    private final int endRow;

    /**
     * 요청 페이지 번호를 기준으로 SQL 조회 범위를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param pageNumber 요청 페이지 번호
     */
    public PageRequest(int pageNumber) {
        this.pageNumber = Math.max(pageNumber, 1);
        this.startRow = (this.pageNumber - 1) * PAGE_SIZE + 1;
        this.endRow = this.pageNumber * PAGE_SIZE;
    }
}

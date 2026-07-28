package org.sadari.admin.sadariadmin.common.pagination;

import lombok.Data;

import java.util.List;

/**
 * fileName       : PageData
 * author         : SeungHyeon.Kang
 * date           : 2026-07-28
 * description    : 목록 데이터와 전체 건수 및 페이지 정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        SeungHyeon.Kang    최초 생성
 */
@Data
public class PageData<T> {

    // 현재 페이지 목록
    private List<T> items;

    // 전체 데이터 건수
    private int totalCount;

    // 현재 페이지 번호
    private int pageNumber;

    // 페이지당 조회 건수
    private int pageSize;

    // 전체 페이지 수
    private int totalPages;

    /**
     * 조회 목록과 전체 건수로 페이지 응답을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param items 현재 페이지 목록
     * @param totalCount 전체 데이터 건수
     * @param pageRequest 페이지 조회 범위
     * @return 목록 페이지 응답
     */
    public static <T> PageData<T> of(List<T> items, int totalCount, PageRequest pageRequest) {
        // 목록 페이지 응답 객체를 생성한다
        PageData<T> pageData = new PageData<>();
        pageData.setItems(items);
        pageData.setTotalCount(totalCount);
        pageData.setPageNumber(pageRequest.getPageNumber());
        pageData.setPageSize(PageRequest.PAGE_SIZE);
        pageData.setTotalPages((int) Math.ceil((double) totalCount / PageRequest.PAGE_SIZE));
        // 계산된 페이지 정보를 포함한 목록 응답을 반환한다
        return pageData;
    }
}

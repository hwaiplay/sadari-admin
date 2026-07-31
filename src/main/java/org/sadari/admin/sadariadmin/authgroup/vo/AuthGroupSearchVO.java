package org.sadari.admin.sadariadmin.authgroup.vo;

import lombok.Data;

/**
 * fileName       : AuthGroupSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-31
 * description    : 권한그룹 목록 검색 조건과 페이지 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        SeungHyeon.Kang    최초 생성
 */
@Data
public class AuthGroupSearchVO {

    // 요청 페이지 번호
    private Integer page = 1;

    // 권한 코드 또는 권한명 검색어
    private String keyword;

    // 사용 여부
    private String useeYsno;

    // 페이지 조회 시작 행 번호
    private int startRow;

    // 페이지 조회 종료 행 번호
    private int endRow;
}

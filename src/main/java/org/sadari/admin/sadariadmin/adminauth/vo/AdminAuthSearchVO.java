package org.sadari.admin.sadariadmin.adminauth.vo;

import lombok.Data;

/**
 * fileName       : AdminAuthSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-31
 * description    : 관리자 권한 목록 검색 조건과 페이지 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        SeungHyeon.Kang    최초 생성
 */
@Data
public class AdminAuthSearchVO {

    // 요청 페이지 번호
    private Integer page = 1;

    // 관리자 번호 또는 아이디 및 이름 검색어
    private String keyword;

    // 부서 코드 검색어
    private String deptCode;

    // 권한그룹 코드
    private String authCode;

    // 페이지 조회 시작 행 번호
    private int startRow;

    // 페이지 조회 종료 행 번호
    private int endRow;
}

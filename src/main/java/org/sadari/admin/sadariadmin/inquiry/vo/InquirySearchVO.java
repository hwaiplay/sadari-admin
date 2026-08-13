package org.sadari.admin.sadariadmin.inquiry.vo;

import lombok.Data;

/**
 * fileName       : InquirySearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 관리자 고객문의 검색 조건을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Data
public class InquirySearchVO {

    // 요청 페이지 번호
    private int page = 1;
    // 고객문의 번호
    private Long inqrNumb;
    // 문의 카테고리 코드
    private String inqrCatg;
    // 문의 처리 상태 코드
    private String inqrStat;
    // 사용자 번호 또는 닉네임 검색어
    private String userKeyword;
    // 조회 시작 행
    private int startRow;
    // 조회 종료 행
    private int endRow;
}

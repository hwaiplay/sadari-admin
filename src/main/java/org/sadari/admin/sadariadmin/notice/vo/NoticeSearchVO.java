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
 * 2026-08-08        SeungHyeon.Kang    목록의 현재 배포 버전 선택 기준 추가
 * 2026-08-08        SeungHyeon.Kang    공지사항 카테고리 검색 조건 추가
 */
@Data
public class NoticeSearchVO {

    // 제목 검색어
    private String keyword;
    // 공지사항 카테고리 상세코드 검색 조건
    private String cateCode;
    // 현재 배포 버전 선택에 사용하는 배포 여부 기준값
    private String dplyYsno;
    // 현재 페이지 번호
    private int page = 1;
    // 조회 시작 행 번호
    private int startRow;
    // 조회 종료 행 번호
    private int endRow;
}

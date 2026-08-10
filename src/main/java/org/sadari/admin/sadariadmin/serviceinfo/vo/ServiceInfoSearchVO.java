package org.sadari.admin.sadariadmin.serviceinfo.vo;

import lombok.Data;

/**
 * fileName       : ServiceInfoSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 서비스 정보 목록 검색 조건과 페이징 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 */
@Data
public class ServiceInfoSearchVO {

    // 제목 검색어
    private String keyword;
    // 서비스 정보 카테고리 검색 조건
    private String cateCode;
    // 현재 배포 버전 선택 기준값
    private String dplyYsno;
    // 현재 페이지 번호
    private int page = 1;
    // 조회 시작 행 번호
    private int startRow;
    // 페이지 조회 건수
    private int endRow;
}

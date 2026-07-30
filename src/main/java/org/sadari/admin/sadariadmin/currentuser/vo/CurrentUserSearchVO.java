package org.sadari.admin.sadariadmin.currentuser.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * fileName       : CurrentUserSearchVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 현재 사용자 목록 검색 조건과 페이지 범위를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@Data
public class CurrentUserSearchVO {

    // 요청 페이지 번호
    private Integer page = 1;

    // 회원번호 또는 닉네임 검색어
    private String keyword;

    // 회원 상태 코드
    private String userStat;

    // 로그인 제공자 코드
    private String userProv;

    // 온보딩 완료 여부
    private String onbdYsno;

    // 가입일 검색 시작일
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate joinDateFrom;

    // 가입일 검색 종료일
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate joinDateTo;

    // 페이지 조회 시작 행 번호
    private int startRow;

    // 페이지 조회 종료 행 번호
    private int endRow;
}

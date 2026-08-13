package org.sadari.admin.sadariadmin.currentuser.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : CurrentUserSuspensionVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 관리자 회원 정지 등록과 해제 및 이력 정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    사용자 서버 동기화 상태 추가
 * 2026-08-13        SeungHyeon.Kang    삭제 회원의 보존 제재 목록과 해제 정보에 재사용
 */
@Data
public class CurrentUserSuspensionVO {

    // 정지 이력 번호
    private Long spndNumb;

    // 정지 대상 회원 번호
    private Long userNumb;

    // 정지 직전 회원 상태
    private String prevStat;

    // 정지 유형
    private String spndType;

    // 정지 유형명
    private String spndTypeName;

    // 정지 사유
    private String spndRson;

    // 정지 사유명
    private String spndRsonName;

    // 관리자 내부 처리 메모
    private String spndCntn;

    // 정지 상태
    private String spndStat;

    // 정지 상태명
    private String spndStatName;

    // 사용자 서버 동기화 상태
    private String syncStat;

    // 정지 시작일
    private LocalDateTime strtDate;

    // 기간 정지 종료일
    private LocalDateTime endxDate;

    // 정지 해제일
    private LocalDateTime rlesDate;

    // 관리자 내부 해제 메모
    private String rlesCntn;

    // 등록 관리자
    private Long regiAdmn;

    // 등록 관리자명
    private String regiAdmnName;

    // 해제 관리자
    private Long rlesAdmn;

    // 해제 관리자명
    private String rlesAdmnName;

    // 등록일
    private LocalDateTime regiDate;

    // 수정 관리자
    private Long updtAdmn;

    // 수정일
    private LocalDateTime updtDate;
}

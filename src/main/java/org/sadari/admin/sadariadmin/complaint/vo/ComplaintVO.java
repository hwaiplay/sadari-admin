package org.sadari.admin.sadariadmin.complaint.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : ComplaintVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 관리자 신고 목록과 상세에 표시할 신고 접수 및 처리 정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintVO {

    // 신고 번호
    private Long cmplNumb;

    // 신고자 회원 번호
    private Long userNumb;

    // 신고자 닉네임
    private String reporterNick;

    // 신고 대상 유형 세부코드
    private String tagtType;

    // 신고 대상 유형명
    private String tagtTypeName;

    // 신고 대상 번호
    private Long tagtNumb;

    // 사용자 신고 대상 닉네임
    private String targetUserNick;

    // 신고 사유 세부코드
    private String cmplRson;

    // 신고 사유명
    private String cmplRsonName;

    // 신고 상세 내용
    private String cmplCntn;

    // 신고 처리 상태 세부코드
    private String cmplStat;

    // 신고 처리 상태명
    private String cmplStatName;

    // 관리자 처리 내용
    private String procCntn;

    // 담당 관리자 번호
    private Long procAdmn;

    // 담당 관리자명
    private String procAdmnName;

    // 최종 처리 일시
    private LocalDateTime procDate;

    // 신고 접수 일시
    private LocalDateTime regiDate;

    // 신고 수정 일시
    private LocalDateTime updtDate;
}

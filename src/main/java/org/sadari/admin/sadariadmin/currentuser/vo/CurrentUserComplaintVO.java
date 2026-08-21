package org.sadari.admin.sadariadmin.currentuser.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : CurrentUserComplaintVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 현재 사용자와 사용자 작성 대상이 받은 신고 이력을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Data
public class CurrentUserComplaintVO {

    // 신고 번호
    private Long cmplNumb;

    // 신고자 사용자 번호
    private Long reporterUserNumb;

    // 신고자 닉네임
    private String reporterNick;

    // 신고 대상 유형 세부코드
    private String tagtType;

    // 신고 대상 유형명
    private String tagtTypeName;

    // 신고 대상 번호
    private Long tagtNumb;

    // 신고 대상 내용 스냅샷
    private String tagtCntn;

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

    // 처리 관리자명
    private String procAdmnName;

    // 처리 일시
    private LocalDateTime procDate;

    // 신고 접수 일시
    private LocalDateTime regiDate;
}

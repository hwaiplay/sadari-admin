package org.sadari.admin.sadariadmin.complaint.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : ComplaintActionVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 동일 신고 대상에 실제 실행된 자동 조치 결과를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintActionVO {

    // 자동 조치 결과 이력 번호
    private Long actnNumb;

    // 신고 대상 유형
    private String tagtType;

    // 신고 대상 번호
    private Long tagtNumb;

    // 자동 조치 시점의 피신고자 회원번호
    private Long tagtUser;

    // 자동 조치 유형 코드
    private String actnType;

    // 자동 조치 유형 명칭
    private String actnTypeName;

    // 자동 조치 결과 코드
    private String rsltCode;

    // 자동 조치 결과 명칭
    private String rsltCodeName;

    // 자동 조치 임계 신고 건수
    private int thrsCntt;

    // 자동 조치 시점의 유효 신고 누적 건수
    private int cmplCntt;

    // 동일 대상의 자동 조치 순번
    private int actnOrdr;

    // 자동 조치를 발생시킨 신고번호
    private Long trigCmpl;

    // 자동 조치 실행 결과 상세
    private String rsltCntn;

    // 자동 조치 실행 일시
    private LocalDateTime regiDate;
}

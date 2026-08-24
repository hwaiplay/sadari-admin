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
 * 2026-08-07        SeungHyeon.Kang    신고자 프로필과 배경 이미지 경로 추가
 * 2026-08-22        SeungHyeon.Kang    신고 대상 내용 스냅샷 추가
 * 2026-08-22        SeungHyeon.Kang    신고 대상 소유 사용자 번호 추가
 * 2026-08-22        SeungHyeon.Kang    신고자 이미지 비노출 정책 반영
 * 2026-08-24        HanWon.Jang        목록 자동조치 담당자 표시 정보 추가
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

    // 신고 대상 버전 SHA-256 해시
    private String tagtHash;

    // 신고 대상 소유 사용자 번호
    private Long tagtUser;

    // 신고 대상 내용 스냅샷
    private String tagtCntn;

    // 관리자 전용 이미지 증거 원본 조회 가능 여부
    private Boolean evidenceAvailable;

    // 신고 대상 소유 사용자 닉네임
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

    // 자동조치 완료 신고 여부
    private Boolean autoActioned;

    // 최종 처리 일시
    private LocalDateTime procDate;

    // 신고 접수 일시
    private LocalDateTime regiDate;

    // 신고 수정 일시
    private LocalDateTime updtDate;
}

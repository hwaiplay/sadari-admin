package org.sadari.admin.sadariadmin.alim.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : AlimTempVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-24
 * description    : TB_ALTEMP 알림 템플릿 VO /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-24        SeungHyeon.Kang    최초 생성
 * 2026-08-12        SeungHyeon.Kang    선택한 알림 아이콘 정보 추가
 * 2026-08-12        SeungHyeon.Kang    알림 상황 기준 아이콘 조회 전환
 * 2026-08-27        SeungHyeon.Kang       감사 관리자 문자열 계약 반영
 */
@Data
public class AlimTempVO {

    /** 알림 상황 코드 */
    private String alimSitu;

    /** 알림 상황 코드명 */
    private String alimSituName;

    /** 템플릿 코드 */
    private String tempCode;

    /** 관리용 제목 */
    private String tempTitl;

    /** 알림 제목 */
    private String alimTitl;

    /** 템플릿 내용 */
    private String tempCont;

    /** 사용 여부 */
    private String useeYsno;

    /** 사용 여부 코드명 */
    private String useeYsnoName;

    /** 등록 관리자 번호 */
    private String regiAdmn;

    /** 등록 관리자명 */
    private String regiAdmnName;

    /** 등록일 */
    private LocalDateTime regiDate;

    /** 수정 관리자 번호 */
    private String updtAdmn;

    /** 수정 관리자명 */
    private String updtAdmnName;

    /** 수정일 */
    private LocalDateTime updtDate;
}

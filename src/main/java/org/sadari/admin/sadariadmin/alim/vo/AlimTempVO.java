package org.sadari.admin.sadariadmin.alim.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * TB_ALTEMP 알림 템플릿 VO
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

    /** 이동 URL */
    private String linkUrlx;

    /** 사용 여부 */
    private String useeYsno;

    /** 사용 여부 코드명 */
    private String useeYsnoName;

    /** 등록 관리자 번호 */
    private Long regiAdmn;

    /** 등록 관리자명 */
    private String regiAdmnName;

    /** 등록일 */
    private LocalDateTime regiDate;

    /** 수정 관리자 번호 */
    private Long updtAdmn;

    /** 수정 관리자명 */
    private String updtAdmnName;

    /** 수정일 */
    private LocalDateTime updtDate;
}

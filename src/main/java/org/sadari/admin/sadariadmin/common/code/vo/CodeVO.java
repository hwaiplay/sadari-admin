package org.sadari.admin.sadariadmin.common.code.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * TB_CODEXD 세부코드 VO
 */
@Data
public class CodeVO {

    /** 공통코드 */
    private String commCode;

    /** 세부코드 */
    private String comdCode;

    /** 세부코드명 */
    private String comdName;

    /** 코드 설명 */
    private String codeExpl;

    /** 사용 여부 */
    private String opt1Code;

    private String opt1Name;

    private String opt2Code;

    private String opt2Name;

    private String opt3Code;

    private String opt3Name;

    private String opt4Code;

    private String opt4Name;

    private String useeYsno;

    private String useeYsnoName;

    /** 등록 관리자 */
    private String regiAdmn;

    private String regiAdmnName;

    private LocalDateTime regiDate;

    private String updtAdmn;

    private String updtAdmnName;

    private LocalDateTime updtDate;

    /** 정렬 순서 */
    private Integer sortOrder;
}

package org.sadari.admin.sadariadmin.common.code.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * TM_CODEXM 공통코드 VO.
 */
@Data
public class CodeMasterVO {

    /** 공통코드. */
    private String commCode;

    /** 공통코드명. */
    private String codeName;

    /** 코드 설명. */
    private String codeExpl;

    /** 사용 여부. */
    private String useeYsno;

    /** 등록 관리자. */
    private String regeAdmn;

    /** 등록일. */
    private LocalDateTime regiDate;
}

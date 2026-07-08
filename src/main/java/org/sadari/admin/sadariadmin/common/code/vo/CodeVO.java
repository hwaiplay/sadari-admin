package org.sadari.admin.sadariadmin.common.code.vo;

import lombok.Data;

/**
 * TB_CODEXD 세부코드 VO.
 */
@Data
public class CodeVO {

    /** 공통코드. */
    private String commCode;

    /** 세부코드. */
    private String comdCode;

    /** 세부코드명. */
    private String comdName;

    /** 코드 설명. */
    private String codeExpl;

    /** 사용 여부. */
    private String useeYsno;

    /** 등록 관리자. */
    private String regeAdmn;

    /** 정렬 순서. */
    private Integer sortOrder;
}

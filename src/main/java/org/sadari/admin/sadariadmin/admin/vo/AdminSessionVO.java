package org.sadari.admin.sadariadmin.admin.vo;

import lombok.Data;

/**
 * 로그인 성공 후 Redis에 저장하는 관리자 세션 VO.
 */
@Data
public class AdminSessionVO {

    /** 관리자 번호. */
    private Long admnNumb;

    /** 관리자 아이디. */
    private String admnIdxx;

    /** 관리자 이름. */
    private String admnName;

    /** 권한 코드. */
    private String authCode;

    /** 권한 레벨. TB_CODEXD.SORT_ORDER 값이다. */
    private Integer authLevel;

    /** 부서 코드. */
    private String deptCode;
}

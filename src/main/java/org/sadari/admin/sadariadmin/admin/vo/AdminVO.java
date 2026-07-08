package org.sadari.admin.sadariadmin.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * TM_ADMINX 관리자 마스터 VO.
 */
@Data
public class AdminVO {

    /** 관리자 번호. */
    private Long admnNumb;

    /** 관리자 아이디. */
    private String admnIdxx;

    /** 관리자 비밀번호 해시. */
    private String passWord;

    /** 관리자 이름. */
    private String admnName;

    /** 권한 코드. */
    private String authCode;

    /** 권한 레벨. TB_CODEXD.SORT_ORDER 값이다. */
    private Integer authLevel;

    /** 부서 코드. */
    private String deptCode;

    /** 로그인 실패 횟수. */
    private Integer failCntx;

    /** 마지막 로그인 일자. */
    private LocalDateTime lginDate;

    /** 등록일. */
    private LocalDateTime regiDate;
}

package org.sadari.admin.sadariadmin.admin.vo;

import lombok.Data;

/**
 * 관리자 로그인 요청 VO.
 */
@Data
public class AdminLoginRequest {

    /** 관리자 아이디. DB 컬럼 규칙 필드명: ADMN_IDXX */
    private String admnIdxx;

    /** 로그인 비밀번호 원문. */
    private String passWord;
}

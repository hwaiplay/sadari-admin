package org.sadari.admin.sadariadmin.admin.vo;

import lombok.Data;

/**
 * 관리자 로그인 요청 VO
 */
@Data
public class AdminLoginRequest {

    /** 관리자 아이디 */
    private String admnIdxx;

    /** 로그인 비밀번호 원문 */
    private String passWord;
}

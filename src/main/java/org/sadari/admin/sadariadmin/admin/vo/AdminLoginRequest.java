package org.sadari.admin.sadariadmin.admin.vo;

import lombok.Data;

/**
 * fileName       : AdminLoginRequest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 관리자 로그인 요청 VO /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 */
@Data
public class AdminLoginRequest {

    /** 관리자 아이디 */
    private String admnIdxx;

    /** 로그인 비밀번호 원문 */
    private String passWord;
}

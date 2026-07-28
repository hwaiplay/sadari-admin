package org.sadari.admin.sadariadmin.admin.vo;

import lombok.Data;

/**
 * fileName       : AdminSessionVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 로그인 성공 후 Redis에 저장하는 관리자 세션 VO /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 */
@Data
public class AdminSessionVO {

    /** 관리자 번호 */
    private Long admnNumb;

    /** 관리자 아이디 */
    private String admnIdxx;

    /** 관리자 이름 */
    private String admnName;

    /** 권한 코드 */
    private String authCode;

    /** 권한 레벨 */
    private Integer authLevel;

    /** 부서 코드 */
    private String deptCode;
}

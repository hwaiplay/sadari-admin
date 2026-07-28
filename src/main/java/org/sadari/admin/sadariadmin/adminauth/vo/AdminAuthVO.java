package org.sadari.admin.sadariadmin.adminauth.vo;

import lombok.Data;

/**
 * fileName       : AdminAuthVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 관리자 권한 부여 VO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@Data
public class AdminAuthVO {

    /** 관리자 번호 */
    private Long admnNumb;

    /** 관리자 아이디 */
    private String admnIdxx;

    /** 관리자명 */
    private String admnName;

    /** 권한 코드 */
    private String authCode;

    /** 권한명 */
    private String authName;

    /** 부서 코드 */
    private String deptCode;
}

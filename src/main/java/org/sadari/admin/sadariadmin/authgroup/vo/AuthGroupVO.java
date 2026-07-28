package org.sadari.admin.sadariadmin.authgroup.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * fileName       : AuthGroupVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 권한그룹 VO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@Data
public class AuthGroupVO {

    /** 권한 코드 */
    private String authCode;

    /** 권한명 */
    private String authName;

    /** 사용 여부 */
    private String useeYsno;

    /** 사용 여부명 */
    private String useeYsnoName;

    /** 등록 관리자 번호 */
    private Long regiAdmn;

    /** 등록 관리자명 */
    private String regiAdmnName;

    /** 등록일 */
    private LocalDateTime regiDate;

    /** 수정 관리자 번호 */
    private Long updtAdmn;

    /** 수정 관리자명 */
    private String updtAdmnName;

    /** 수정일 */
    private LocalDateTime updtDate;

    /** 메뉴별 권한 목록 */
    private List<AuthMenuVO> menus;
}

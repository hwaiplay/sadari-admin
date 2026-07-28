package org.sadari.admin.sadariadmin.usermenu.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : UserMenuVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : TM_URMENU 사용자 메뉴 관리 VO /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@Data
public class UserMenuVO {

    /** 메뉴 번호 */
    private String menuNumb;

    /** 하위 메뉴 번호 */
    private String subxNumb;

    /** 메뉴명 */
    private String menuName;

    /** 메뉴 URL */
    private String menuUrlx;

    /** 정렬 순서 */
    private Integer sortOrdr;

    /** 노출 여부 */
    private String showYsno;

    /** 노출 여부명 */
    private String showYsnoName;

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
}

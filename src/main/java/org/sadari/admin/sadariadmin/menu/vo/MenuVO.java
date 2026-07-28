package org.sadari.admin.sadariadmin.menu.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : MenuVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : TM_ADMENU 관리자 메뉴 관리 VO /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 */
@Data
public class MenuVO {

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

    /** 사용 여부 */
    private String useeYsno;

    private String useeYsnoName;

    /** 등록 관리자 번호 */
    private Long regiAdmn;

    private String regiAdmnName;

    /** 등록일 */
    private LocalDateTime regiDate;

    /** 수정 관리자 번호 */
    private Long updtAdmn;

    private String updtAdmnName;

    /** 수정일 */
    private LocalDateTime updtDate;
}

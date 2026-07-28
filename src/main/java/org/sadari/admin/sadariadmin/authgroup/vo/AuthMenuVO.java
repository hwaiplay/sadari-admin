package org.sadari.admin.sadariadmin.authgroup.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : AuthMenuVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 메뉴별 권한 VO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@Data
public class AuthMenuVO {

    /** 권한 코드 */
    private String authCode;

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

    /** 조회 권한 여부 */
    private String readYsno;

    /** 쓰기 권한 여부 */
    private String writYsno;

    /** 삭제 권한 여부 */
    private String deltYsno;

    /** 등록 관리자 번호 */
    private Long regiAdmn;

    /** 등록일 */
    private LocalDateTime regiDate;

    /** 수정 관리자 번호 */
    private Long updtAdmn;

    /** 수정일 */
    private LocalDateTime updtDate;
}

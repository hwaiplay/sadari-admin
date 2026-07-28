package org.sadari.admin.sadariadmin.menu.vo;

import lombok.Data;

/**
 * fileName       : MenuPermissionVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 관리자 메뉴 권한 VO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 */
@Data
public class MenuPermissionVO {

    /** 조회 권한 여부 */
    private String readYsno;

    /** 쓰기 권한 여부 */
    private String writYsno;

    /** 삭제 권한 여부 */
    private String deltYsno;
}

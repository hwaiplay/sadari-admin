package org.sadari.admin.sadariadmin.menu.vo;

import lombok.Data;

/** 관리자 메뉴 권한 VO */
@Data
public class MenuPermissionVO {

    /** 조회 권한 여부 */
    private String readYsno;

    /** 쓰기 권한 여부 */
    private String writYsno;

    /** 삭제 권한 여부 */
    private String deltYsno;
}

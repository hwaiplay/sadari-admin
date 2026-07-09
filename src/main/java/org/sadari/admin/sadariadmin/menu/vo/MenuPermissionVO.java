package org.sadari.admin.sadariadmin.menu.vo;

import lombok.Data;

/**
 * 메뉴별 권한 레벨 VO
 */
@Data
public class MenuPermissionVO {

    /** 메뉴 URL */
    private String menuUrlx;

    /** 조회 권한 레벨 */
    private Integer readLevel;

    /** 쓰기 권한 레벨 */
    private Integer writLevel;

    /** 삭제 권한 레벨 */
    private Integer deltLevel;
}

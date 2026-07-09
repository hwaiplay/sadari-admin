package org.sadari.admin.sadariadmin.menu.vo;

import lombok.Data;

/**
 * 로그인 관리자 메뉴 권한 결과 VO
 */
@Data
public class MenuPermissionResultVO {

    /** 조회 가능 여부 */
    private boolean readYn;

    /** 쓰기 가능 여부 */
    private boolean writYn;

    /** 삭제 가능 여부 */
    private boolean deltYn;
}

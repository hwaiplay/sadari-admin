package org.sadari.admin.sadariadmin.menu.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * TM_MENUXM 메뉴 관리 VO.
 */
@Data
public class MenuVO {

    /** 메뉴 번호. 상위 메뉴별 묶음 번호이다. */
    private String menuNumb;

    /** 하위 메뉴 번호. 상위 메뉴는 0, 하위 메뉴는 1부터 증가한다. */
    private String subxNumb;

    /** 메뉴명. */
    private String menuName;

    /** 메뉴 URL. */
    private String menuUrlx;

    /** 조회 권한 코드. */
    private String readAuth;

    /** 쓰기 권한 코드. */
    private String writAuth;

    /** 삭제 권한 코드. */
    private String deltAuth;

    /** 정렬 순서. */
    private Integer sortOrdr;

    /** 사용 여부. */
    private String useeYsno;

    /** 등록 관리자 번호. */
    private Long regiAdmn;

    /** 등록일. */
    private LocalDateTime regiDate;

    /** 수정 관리자 번호. */
    private Long modxAdmn;

    /** 수정일. */
    private LocalDateTime modxDate;
}

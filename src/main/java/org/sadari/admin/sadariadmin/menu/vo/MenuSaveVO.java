package org.sadari.admin.sadariadmin.menu.vo;

import lombok.Data;

import java.util.List;

/**
 * fileName       : MenuSaveVO
 * author         : SeungHyeon.Kang
 * date           : 2026-10-12
 * description    : 메뉴 상세 화면의 상위 메뉴와 기존 및 신규 하위 메뉴를 한 요청으로 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-10-12        SeungHyeon.Kang    최초 생성
 */
@Data
public class MenuSaveVO {

    // 저장할 기준 메뉴
    private MenuVO menu;

    // 수정할 기존 하위 메뉴
    private List<MenuVO> subMenus;

    // 등록할 신규 하위 메뉴
    private List<MenuVO> childMenus;
}

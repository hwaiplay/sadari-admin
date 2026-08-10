package org.sadari.admin.sadariadmin.usermenu.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.usermenu.service.UserMenuService;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuSearchVO;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : UserMenuController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 관리 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    사용자 메뉴 목록 검색 조건 추가
 * 2026-08-10        SeungHyeon.Kang    단일 메뉴 번호 기반 API 적용
 * 2026-08-10        SeungHyeon.Kang    사용자 메뉴 직계 하위 목록 API 추가
 */
@RestController
@RequestMapping(Constant.API_USER_MENUS_PREFIX)
public class UserMenuController {

    /** 사용자 메뉴 관리 서비스 */
    private final UserMenuService userMenuService;

    /** 사용자 메뉴 관리 API 컨트롤러를 생성한다. */
    public UserMenuController(UserMenuService userMenuService) {
        // 사용자 메뉴 관리 서비스 의존성을 설정한다
        this.userMenuService = userMenuService;
    }

    /** 사용자 메뉴 목록을 조회한다. */
    @GetMapping
    public ResultData getUserMenuList(@ModelAttribute UserMenuSearchVO search,
                                      @AuthenticationPrincipal AdminSessionVO admin) {
        // 검색 조건에 맞는 사용자 메뉴 목록을 반환한다
        return ResultData.success(userMenuService.getUserMenuList(search, admin));
    }

    /** 사용자 메뉴의 상위 메뉴 후보 목록을 조회한다. */
    @GetMapping("/parents")
    public ResultData getUserMenuParentList(@AuthenticationPrincipal AdminSessionVO admin) {
        // 1단계와 2단계 사용자 메뉴를 상위 메뉴 후보로 반환한다
        return ResultData.success(userMenuService.getUserMenuParentList(admin));
    }

    /** 사용자 메뉴 상세를 조회한다. */
    @GetMapping("/{menuNumb}")
    public ResultData getUserMenuDtl(@PathVariable Long menuNumb,
                                     @AuthenticationPrincipal AdminSessionVO admin) {
        // 메뉴 번호에 해당하는 사용자 메뉴 상세를 반환한다
        return ResultData.success(userMenuService.getUserMenuDtl(menuNumb, admin));
    }

    /** 사용자 메뉴의 직계 하위 메뉴 목록을 조회한다. */
    @GetMapping("/{menuNumb}/children")
    public ResultData getUserMenuChildList(@PathVariable Long menuNumb,
                                           @AuthenticationPrincipal AdminSessionVO admin) {
        // 선택한 사용자 메뉴 바로 아래의 메뉴 목록을 반환한다
        return ResultData.success(userMenuService.getUserMenuChildList(menuNumb, admin));
    }

    /** 사용자 메뉴를 등록한다. */
    @PostMapping
    public ResultData setUserMenu(@RequestBody UserMenuVO menu,
                                  @AuthenticationPrincipal AdminSessionVO admin) {
        // 신규 사용자 메뉴를 저장하고 생성된 메뉴를 반환한다
        return ResultData.success(ResultEnum.SAVE_SUCCESS, userMenuService.setUserMenu(menu, admin));
    }

    /** 사용자 메뉴를 수정한다. */
    @PutMapping("/{menuNumb}")
    public ResultData uptUserMenu(@PathVariable Long menuNumb,
                                  @RequestBody UserMenuVO menu,
                                  @AuthenticationPrincipal AdminSessionVO admin) {
        // 경로의 메뉴 번호를 수정 대상에 설정한다
        menu.setMenuNumb(menuNumb);
        // 사용자 메뉴를 수정하고 최신 상세를 반환한다
        return ResultData.success(ResultEnum.UPDATE_SUCCESS, userMenuService.uptUserMenu(menu, admin));
    }

    /** 사용자 메뉴를 삭제한다. */
    @DeleteMapping("/{menuNumb}")
    public ResultData delUserMenu(@PathVariable Long menuNumb,
                                  @AuthenticationPrincipal AdminSessionVO admin) {
        // 자식 메뉴가 없는 사용자 메뉴를 삭제한다
        userMenuService.delUserMenu(menuNumb, admin);
        // 사용자 메뉴 삭제 성공 응답을 반환한다
        return ResultData.success(ResultEnum.DELETE_SUCCESS);
    }
}

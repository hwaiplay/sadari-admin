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
 * description    : 사용자 메뉴 관리 API 컨트롤러
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    사용자 메뉴 목록 검색 조건 추가
 */
@RestController
@RequestMapping(Constant.API_USER_MENUS_PREFIX)
public class UserMenuController {

    /** 사용자 메뉴 관리 서비스 */
    private final UserMenuService userMenuService;

    /** 사용자 메뉴 관리 API 컨트롤러 생성 */
    public UserMenuController(UserMenuService userMenuService) {
        this.userMenuService = userMenuService;
    }

    /** 사용자 상위 메뉴 목록 조회 */
    @GetMapping
    public ResultData getUserMenuList(@ModelAttribute UserMenuSearchVO search
                                    , @AuthenticationPrincipal AdminSessionVO admin) {
        // 검색 조건에 맞는 사용자 메뉴 목록을 반환한다
        return ResultData.success(userMenuService.getUserMenuList(search, admin));
    }

    /** 사용자 메뉴 상세 조회 */
    @GetMapping("/{menuNumb}/{subxNumb}")
    public ResultData getUserMenuDtl(@PathVariable String menuNumb, @PathVariable String subxNumb, @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(userMenuService.getUserMenuDtl(menuNumb, subxNumb, admin));
    }

    /** 사용자 하위 메뉴 목록 조회 */
    @GetMapping("/{menuNumb}/children")
    public ResultData getUserSubMenuList(@PathVariable String menuNumb, @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(userMenuService.getUserSubMenuList(menuNumb, admin));
    }

    /** 사용자 메뉴 등록 */
    @PostMapping
    public ResultData setUserMenu(@RequestBody UserMenuVO menu, @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(ResultEnum.SAVE_SUCCESS, userMenuService.setUserMenu(menu, admin));
    }

    /** 사용자 메뉴 수정 */
    @PutMapping("/{menuNumb}/{subxNumb}")
    public ResultData uptUserMenu(@PathVariable String menuNumb, @PathVariable String subxNumb, @RequestBody UserMenuVO menu, @AuthenticationPrincipal AdminSessionVO admin) {
        menu.setMenuNumb(menuNumb);
        menu.setSubxNumb(subxNumb);
        return ResultData.success(ResultEnum.UPDATE_SUCCESS, userMenuService.uptUserMenu(menu, admin));
    }

    /** 사용자 메뉴 삭제 */
    @DeleteMapping("/{menuNumb}/{subxNumb}")
    public ResultData delUserMenu(@PathVariable String menuNumb, @PathVariable String subxNumb, @AuthenticationPrincipal AdminSessionVO admin) {
        userMenuService.delUserMenu(menuNumb, subxNumb, admin);
        return ResultData.success(ResultEnum.DELETE_SUCCESS);
    }
}

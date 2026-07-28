package org.sadari.admin.sadariadmin.menu.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.menu.service.MenuService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : MenuPermissionController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 관리자 메뉴 권한 조회 API
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 */
@RestController
public class MenuPermissionController {

    /** 메뉴 관리 서비스 */
    private final MenuService menuService;

    /** 관리자 메뉴 권한 조회 API 생성 */
    public MenuPermissionController(MenuService menuService) {
        this.menuService = menuService;
    }

    /** 로그인 관리자의 메뉴 권한 조회 */
    @GetMapping("/api/menu-permissions")
    public ResultData getMenuPermission(
            @RequestParam String menuUrlx,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(menuService.getMenuPermission(menuUrlx, admin));
    }
}

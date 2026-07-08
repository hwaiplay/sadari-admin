package org.sadari.admin.sadariadmin.menu.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.menu.service.MenuPermissionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메뉴 권한 조회 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/menu-permissions")
public class MenuPermissionController {

    /** 메뉴 권한 서비스. */
    private final MenuPermissionService menuPermissionService;

    public MenuPermissionController(MenuPermissionService menuPermissionService) {
        this.menuPermissionService = menuPermissionService;
    }

    /**
     * 로그인 관리자의 메뉴 권한을 조회한다.
     */
    @GetMapping
    public ResultData getPermission(
            @RequestParam String menuUrlx,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(menuPermissionService.getPermission(menuUrlx, admin));
    }
}

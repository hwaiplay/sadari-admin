package org.sadari.admin.sadariadmin.menu.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.menu.service.MenuService;
import org.sadari.admin.sadariadmin.menu.vo.MenuVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 메뉴 관리 API 컨트롤러. */
@RestController
@RequestMapping("/api/menus")
public class MenuController {

    /** 메뉴 관리 서비스. */
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /** Redis 권한 레벨 기준으로 사이드바 메뉴 목록을 조회한다. */
    @GetMapping("/sidebar")
    public ResultData getMenuList(@AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(menuService.getMenuList(admin.getAuthLevel()));
    }

    /** 메뉴관리 목록을 조회한다. */
    @GetMapping
    public ResultData getMenuMngList(@AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(menuService.getMenuMngList(admin));
    }

    /** 메뉴 상세를 조회한다. */
    @GetMapping("/{menuNumb}/{subxNumb}")
    public ResultData getMenuDtl(
            @PathVariable String menuNumb,
            @PathVariable String subxNumb,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(menuService.getMenuDtl(menuNumb, subxNumb, admin));
    }

    /** 하위 메뉴 목록을 조회한다. */
    @GetMapping("/{menuNumb}/children")
    public ResultData getSubMenuList(
            @PathVariable String menuNumb,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(menuService.getSubMenuList(menuNumb, admin));
    }

    /** 메뉴를 등록한다. */
    @PostMapping
    public ResultData setMenu(@RequestBody MenuVO menu, @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(menuService.setMenu(menu, admin));
    }

    /** 메뉴를 수정한다. */
    @PutMapping("/{menuNumb}/{subxNumb}")
    public ResultData uptMenu(
            @PathVariable String menuNumb,
            @PathVariable String subxNumb,
            @RequestBody MenuVO menu,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        menu.setMenuNumb(menuNumb);
        menu.setSubxNumb(subxNumb);
        return ResultData.success(menuService.uptMenu(menu, admin));
    }

    /** 메뉴를 삭제한다. */
    @DeleteMapping("/{menuNumb}/{subxNumb}")
    public ResultData delMenu(
            @PathVariable String menuNumb,
            @PathVariable String subxNumb,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        menuService.delMenu(menuNumb, subxNumb, admin);
        return ResultData.success();
    }
}

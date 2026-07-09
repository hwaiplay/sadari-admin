package org.sadari.admin.sadariadmin.menu.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
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

/** 메뉴 관리 API 컨트롤러 */
@RestController
@RequestMapping(Constant.API_MENUS_PREFIX)
public class MenuController {

    /** 메뉴 관리 서비스 */
    private final MenuService menuService;

    /**
     * 메뉴 관리 API 컨트롤러 생성
     * @Author SeungHyeon.Kang
     * @param menuService
     * @return
     */
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 사이드바 메뉴 목록 조회
     * @Author SeungHyeon.Kang
     * @param admin
     * @return
     */
    @GetMapping("/sidebar")
    public ResultData getMenuList(@AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(menuService.getMenuList(admin.getAuthLevel()));
    }

    /**
     * 메뉴관리 목록 조회
     * @Author SeungHyeon.Kang
     * @param admin
     * @return
     */
    @GetMapping
    public ResultData getMenuMngList(@AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(menuService.getMenuMngList(admin));
    }

    /**
     * 메뉴 상세 조회
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @param admin
     * @return
     */
    @GetMapping("/{menuNumb}/{subxNumb}")
    public ResultData getMenuDtl(
            @PathVariable String menuNumb,
            @PathVariable String subxNumb,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(menuService.getMenuDtl(menuNumb, subxNumb, admin));
    }

    /**
     * 하위 메뉴 목록 조회
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param admin
     * @return
     */
    @GetMapping("/{menuNumb}/children")
    public ResultData getSubMenuList(
            @PathVariable String menuNumb,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(menuService.getSubMenuList(menuNumb, admin));
    }

    /**
     * 메뉴 등록
     * @Author SeungHyeon.Kang
     * @param menu
     * @param admin
     * @return
     */
    @PostMapping
    public ResultData setMenu(@RequestBody MenuVO menu, @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(ResultEnum.SAVE_SUCCESS, menuService.setMenu(menu, admin));
    }

    /**
     * 메뉴 수정
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @param menu
     * @param admin
     * @return
     */
    @PutMapping("/{menuNumb}/{subxNumb}")
    public ResultData uptMenu(
            @PathVariable String menuNumb,
            @PathVariable String subxNumb,
            @RequestBody MenuVO menu,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        menu.setMenuNumb(menuNumb);
        menu.setSubxNumb(subxNumb);
        return ResultData.success(ResultEnum.UPDATE_SUCCESS, menuService.uptMenu(menu, admin));
    }

    /**
     * 메뉴 삭제
     * @Author SeungHyeon.Kang
     * @param menuNumb
     * @param subxNumb
     * @param admin
     * @return
     */
    @DeleteMapping("/{menuNumb}/{subxNumb}")
    public ResultData delMenu(
            @PathVariable String menuNumb,
            @PathVariable String subxNumb,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        menuService.delMenu(menuNumb, subxNumb, admin);
        return ResultData.success(ResultEnum.DELETE_SUCCESS);
    }
}

package org.sadari.admin.sadariadmin.menu.service.impl;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.menu.mapper.MenuMapper;
import org.sadari.admin.sadariadmin.menu.service.MenuService;
import org.sadari.admin.sadariadmin.menu.vo.MenuVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** 메뉴 관리 서비스 구현체. */
@Service
public class MenuServiceImpl implements MenuService {

    /** 관리자 메뉴 Mapper. */
    private final MenuMapper menuMapper;

    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    public List<MenuVO> getMenuList(Integer authLevel) {
        return menuMapper.getMenuList(authLevel);
    }

    @Override
    public List<MenuVO> getMenuMngList(AdminSessionVO admin) {
        checkLogin(admin);
        return menuMapper.getMenuMngList();
    }

    @Override
    public MenuVO getMenuDtl(String menuNumb, String subxNumb, AdminSessionVO admin) {
        checkLogin(admin);
        MenuVO menu = menuMapper.getMenuDtl(menuNumb, subxNumb);
        if (menu == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다.");
        }
        return menu;
    }

    @Override
    public List<MenuVO> getSubMenuList(String menuNumb, AdminSessionVO admin) {
        checkLogin(admin);
        return menuMapper.getSubMenuList(menuNumb);
    }

    @Override
    @Transactional
    public MenuVO setMenu(MenuVO menu, AdminSessionVO admin) {
        checkLogin(admin);
        setMenuKey(menu);
        setMenuDefault(menu, admin);
        menuMapper.setMenu(menu);
        return menuMapper.getMenuDtl(menu.getMenuNumb(), menu.getSubxNumb());
    }

    @Override
    @Transactional
    public MenuVO uptMenu(MenuVO menu, AdminSessionVO admin) {
        checkLogin(admin);
        setMenuDefault(menu, admin);
        menu.setModxAdmn(admin.getAdmnNumb());
        menuMapper.uptMenu(menu);
        return menuMapper.getMenuDtl(menu.getMenuNumb(), menu.getSubxNumb());
    }

    @Override
    @Transactional
    public void delMenu(String menuNumb, String subxNumb, AdminSessionVO admin) {
        checkLogin(admin);
        menuMapper.delMenu(menuNumb, subxNumb);
    }

    /** 등록 메뉴의 복합키를 만든다. */
    private void setMenuKey(MenuVO menu) {
        if (menu.getMenuNumb() == null || menu.getMenuNumb().isBlank()) {
            menu.setMenuNumb(menuMapper.getMenuNumb());
            menu.setSubxNumb("0");
            return;
        }

        if (menu.getSubxNumb() == null || menu.getSubxNumb().isBlank()) {
            menu.setSubxNumb(menuMapper.getSubxNumb(menu.getMenuNumb()));
        }
    }

    /** 메뉴 저장에 필요한 기본값을 채운다. */
    private void setMenuDefault(MenuVO menu, AdminSessionVO admin) {
        if (menu.getReadAuth() == null || menu.getReadAuth().isBlank()) {
            menu.setReadAuth(admin.getAuthCode());
        }
        if (menu.getWritAuth() == null || menu.getWritAuth().isBlank()) {
            menu.setWritAuth(menu.getReadAuth());
        }
        if (menu.getDeltAuth() == null || menu.getDeltAuth().isBlank()) {
            menu.setDeltAuth(menu.getReadAuth());
        }
        if (menu.getUseeYsno() == null || menu.getUseeYsno().isBlank()) {
            menu.setUseeYsno("Y");
        }
        if (menu.getSortOrdr() == null) {
            menu.setSortOrdr(10);
        }
        menu.setRegiAdmn(admin.getAdmnNumb());
    }

    /** 로그인 상태를 확인한다. */
    private void checkLogin(AdminSessionVO admin) {
        if (admin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }
}

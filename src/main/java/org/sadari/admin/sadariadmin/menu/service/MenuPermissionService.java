package org.sadari.admin.sadariadmin.menu.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.menu.mapper.MenuMapper;
import org.sadari.admin.sadariadmin.menu.vo.MenuPermissionResultVO;
import org.sadari.admin.sadariadmin.menu.vo.MenuPermissionVO;
import org.springframework.stereotype.Service;

/**
 * 메뉴별 조회, 쓰기, 삭제 권한을 계산하는 서비스.
 */
@Service
public class MenuPermissionService {

    /** 메뉴 Mapper. */
    private final MenuMapper menuMapper;

    public MenuPermissionService(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    /**
     * 메뉴 URL 기준으로 로그인 관리자의 권한 결과를 조회한다.
     */
    public MenuPermissionResultVO getPermission(String menuUrlx, AdminSessionVO admin) {
        MenuPermissionVO permission = menuMapper.getMenuPermission(menuUrlx);
        MenuPermissionResultVO result = new MenuPermissionResultVO();
        result.setReadYn(hasLevel(admin, permission == null ? null : permission.getReadLevel()));
        result.setWritYn(hasLevel(admin, permission == null ? null : permission.getWritLevel()));
        result.setDeltYn(hasLevel(admin, permission == null ? null : permission.getDeltLevel()));
        return result;
    }

    /**
     * 조회 권한 여부를 확인한다.
     */
    public boolean hasRead(String menuUrlx, AdminSessionVO admin) {
        return getPermission(menuUrlx, admin).isReadYn();
    }

    /**
     * 쓰기 권한 여부를 확인한다.
     */
    public boolean hasWrite(String menuUrlx, AdminSessionVO admin) {
        return getPermission(menuUrlx, admin).isWritYn();
    }

    /**
     * 삭제 권한 여부를 확인한다.
     */
    public boolean hasDelete(String menuUrlx, AdminSessionVO admin) {
        return getPermission(menuUrlx, admin).isDeltYn();
    }

    /**
     * 관리자 권한 레벨이 메뉴 요구 레벨 이상인지 확인한다.
     */
    private boolean hasLevel(AdminSessionVO admin, Integer requiredLevel) {
        return admin != null
                && admin.getAuthLevel() != null
                && requiredLevel != null
                && requiredLevel <= admin.getAuthLevel();
    }
}

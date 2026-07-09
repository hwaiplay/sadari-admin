package org.sadari.admin.sadariadmin.menu.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.menu.mapper.MenuMapper;
import org.sadari.admin.sadariadmin.menu.vo.MenuPermissionResultVO;
import org.sadari.admin.sadariadmin.menu.vo.MenuPermissionVO;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.springframework.stereotype.Service;

/**
 * 메뉴별 조회 쓰기 삭제 권한 계산 서비스
 */
@Service
public class MenuPermissionService {

    /** 메뉴 Mapper */
    private final MenuMapper menuMapper;

    /**
     * 메뉴 권한 서비스 생성
     * @Author SeungHyeon.Kang
     * @param menuMapper
     * @return
     */
    public MenuPermissionService(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    /**
     * 메뉴 권한 조회
     * @Author SeungHyeon.Kang
     * @param menuUrlx
     * @param admin
     * @return
     */
    public MenuPermissionResultVO getPermission(String menuUrlx, AdminSessionVO admin) {
        MenuPermissionVO permission = menuMapper.getMenuPermission(menuUrlx);
        MenuPermissionResultVO result = new MenuPermissionResultVO();
        result.setReadYn(hasLevel(admin, StringUtil.isEmpty(permission) ? null : permission.getReadLevel()));
        result.setWritYn(hasLevel(admin, StringUtil.isEmpty(permission) ? null : permission.getWritLevel()));
        result.setDeltYn(hasLevel(admin, StringUtil.isEmpty(permission) ? null : permission.getDeltLevel()));
        return result;
    }

    /**
     * 조회 권한 여부 확인
     * @Author SeungHyeon.Kang
     * @param menuUrlx
     * @param admin
     * @return
     */
    public boolean hasRead(String menuUrlx, AdminSessionVO admin) {
        return getPermission(menuUrlx, admin).isReadYn();
    }

    /**
     * 쓰기 권한 여부 확인
     * @Author SeungHyeon.Kang
     * @param menuUrlx
     * @param admin
     * @return
     */
    public boolean hasWrite(String menuUrlx, AdminSessionVO admin) {
        return getPermission(menuUrlx, admin).isWritYn();
    }

    /**
     * 삭제 권한 여부 확인
     * @Author SeungHyeon.Kang
     * @param menuUrlx
     * @param admin
     * @return
     */
    public boolean hasDelete(String menuUrlx, AdminSessionVO admin) {
        return getPermission(menuUrlx, admin).isDeltYn();
    }

    /**
     * 관리자 권한 레벨 충족 여부 확인
     * @Author SeungHyeon.Kang
     * @param admin
     * @param requiredLevel
     * @return
     */
    private boolean hasLevel(AdminSessionVO admin, Integer requiredLevel) {
        // 로그인 관리자 권한 레벨이 메뉴 요구 레벨 이상일 때만 권한 보유로 판단한다
        return !StringUtil.isEmpty(admin)
                && !StringUtil.isEmpty(admin.getAuthLevel())
                && !StringUtil.isEmpty(requiredLevel)
                && requiredLevel <= admin.getAuthLevel();
    }
}

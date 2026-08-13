package org.sadari.admin.sadariadmin.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.menu.mapper.MenuMapper;
import org.sadari.admin.sadariadmin.menu.vo.MenuPermissionVO;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * fileName       : MenuPermissionInterceptor
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 관리자 메뉴 API 권한 인터셉터
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    권한그룹 목록 화면 URL 변경
 * 2026-07-28        SeungHyeon.Kang    스케줄러 로그 조회 권한 연결
 * 2026-07-30        SeungHyeon.Kang    팝업 콘텐츠 관리 권한 연결
 * 2026-07-30        SeungHyeon.Kang    현재 사용자 조회 권한 연결
 * 2026-08-05        SeungHyeon.Kang    신고 관리 권한 연결
 * 2026-08-12        SeungHyeon.Kang    알림 아이콘 관리 권한 연결
 * 2026-08-13        SeungHyeon.Kang    사용자 통계 조회 권한 연결
 */
@Component
public class MenuPermissionInterceptor implements HandlerInterceptor {

    /** API 경로와 관리자 화면 URL 매핑 */
    private static final Map<String, String> MENU_URL_BY_API = new LinkedHashMap<>();

    static {
        MENU_URL_BY_API.put(Constant.API_ADMIN_AUTHS_PREFIX, "/sadari/adm/admin/authAdmin");
        MENU_URL_BY_API.put(Constant.API_SCHEDULE_LOGS_PREFIX, "/sadari/adm/scheduleLog/list");
        MENU_URL_BY_API.put(Constant.API_AUTH_GROUP_PREFIX, "/sadari/adm/admin/auth/list");
        MENU_URL_BY_API.put(Constant.API_USER_MENUS_PREFIX, "/sadari/adm/userMenu/list");
        MENU_URL_BY_API.put(Constant.API_CODE_MANAGE_PREFIX, "/sadari/adm/code/list");
        MENU_URL_BY_API.put(Constant.API_ALIM_TEMP_PREFIX, "/sadari/adm/alimTemp/list");
        MENU_URL_BY_API.put(Constant.API_ALIM_ICON_PREFIX, "/sadari/adm/alimIcon/list");
        MENU_URL_BY_API.put(Constant.API_POPUP_CONTENT_PREFIX, "/sadari/adm/popup/list");
        MENU_URL_BY_API.put(Constant.API_CURRENT_USERS_PREFIX, "/sadari/adm/user/list");
        MENU_URL_BY_API.put(Constant.API_USER_STATISTICS_PREFIX, "/sadari/adm/statistics");
        MENU_URL_BY_API.put(Constant.API_COMPLAINTS_PREFIX, "/sadari/adm/complaint/list");
        MENU_URL_BY_API.put(Constant.API_INQUIRIES_PREFIX, "/sadari/adm/inquiry");
        MENU_URL_BY_API.put(Constant.API_MENUS_PREFIX, "/sadari/adm/menu/list");
    }

    /** 메뉴 Mapper */
    private final MenuMapper menuMapper;

    /** 관리자 메뉴 API 권한 인터셉터 생성 */
    public MenuPermissionInterceptor(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    /** HTTP 메서드에 맞는 메뉴 권한 확인 */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (Constant.API_MENU_SIDEBAR.equals(uri) || Constant.API_MENU_PERMISSION.equals(uri)) {
            return true;
        }

        String menuUrlx = findMenuUrl(uri);
        if (StringUtil.isEmpty(menuUrlx)) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (StringUtil.isEmpty(authentication) || !(authentication.getPrincipal() instanceof AdminSessionVO admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }

        MenuPermissionVO permission = menuMapper.getMenuPermission(admin.getAuthCode(), menuUrlx);
        if (!hasPermission(permission, request.getMethod())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, ResultEnum.FORBIDDEN);
        }
        return true;
    }

    /** API 경로에 해당하는 메뉴 URL 조회 */
    private String findMenuUrl(String uri) {
        return MENU_URL_BY_API.entrySet().stream()
                .filter(entry -> uri.equals(entry.getKey()) || uri.startsWith(entry.getKey() + "/"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /** HTTP 메서드별 권한 여부 확인 */
    private boolean hasPermission(MenuPermissionVO permission, String method) {
        if (StringUtil.isEmpty(permission)) {
            return false;
        }
        if (HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method) || HttpMethod.OPTIONS.matches(method)) {
            return Constant.YES.equals(permission.getReadYsno());
        }
        if (HttpMethod.DELETE.matches(method)) {
            return Constant.YES.equals(permission.getDeltYsno());
        }
        return Constant.YES.equals(permission.getWritYsno());
    }
}

package org.sadari.admin.sadariadmin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.menu.service.MenuPermissionService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * API 요청을 메뉴 권한으로 제어하는 Spring Security 필터.
 */
@Component
public class MenuAuthorizationFilter extends OncePerRequestFilter {

    private static final String MENU_MNG_URL = "/sadari/adm/menu/list";
    private static final String CODE_MNG_URL = "/sadari/adm/code/list";

    /** 메뉴 권한 서비스. */
    private final MenuPermissionService menuPermissionService;

    public MenuAuthorizationFilter(MenuPermissionService menuPermissionService) {
        this.menuPermissionService = menuPermissionService;
    }

    /**
     * 메뉴 권한으로 제어할 API만 필터링한다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/api/menus") && !uri.startsWith("/api/code-manage");
    }

    /**
     * 요청 API와 HTTP 메서드에 맞는 메뉴 권한을 확인한다.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if ("/api/menus/sidebar".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        AdminSessionVO admin = getAdmin();
        String menuUrlx = getMenuUrlx(request);
        if (menuUrlx == null || !hasPermission(request, menuUrlx, admin)) {
            writeResult(response, HttpServletResponse.SC_FORBIDDEN, ResultData.fail(ResultEnum.FORBIDDEN));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * SecurityContext에서 로그인 관리자 정보를 조회한다.
     */
    private AdminSessionVO getAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminSessionVO admin)) {
            return null;
        }
        return admin;
    }

    /**
     * API 경로를 메뉴 URL로 변환한다.
     */
    private String getMenuUrlx(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/menus")) {
            return MENU_MNG_URL;
        }
        if (uri.startsWith("/api/code-manage")) {
            return CODE_MNG_URL;
        }
        return null;
    }

    /**
     * HTTP 메서드에 따라 조회, 쓰기, 삭제 권한을 확인한다.
     */
    private boolean hasPermission(HttpServletRequest request, String menuUrlx, AdminSessionVO admin) {
        return switch (request.getMethod()) {
            case "GET" -> menuPermissionService.hasRead(menuUrlx, admin);
            case "POST", "PUT", "PATCH" -> menuPermissionService.hasWrite(menuUrlx, admin);
            case "DELETE" -> menuPermissionService.hasDelete(menuUrlx, admin);
            default -> false;
        };
    }

    /**
     * Spring Security 권한 실패 응답을 공통 응답으로 작성한다.
     */
    private void writeResult(HttpServletResponse response, int status, ResultData resultData) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format(
                "{\"code\":%d,\"message\":\"%s\",\"data\":null}",
                resultData.getCode(),
                resultData.getMessage()
        ));
    }
}

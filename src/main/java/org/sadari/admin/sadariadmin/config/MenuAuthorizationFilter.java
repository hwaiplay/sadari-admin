package org.sadari.admin.sadariadmin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.menu.service.MenuPermissionService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 메뉴 권한 기반 API 접근 제어 필터
 */
@Component
public class MenuAuthorizationFilter extends OncePerRequestFilter {

    /** 메뉴 권한 서비스 */
    private final MenuPermissionService menuPermissionService;

    /**
     * 메뉴 권한 필터 생성
     * @Author SeungHyeon.Kang
     * @param menuPermissionService
     * @return
     */
    public MenuAuthorizationFilter(MenuPermissionService menuPermissionService) {
        this.menuPermissionService = menuPermissionService;
    }

    /**
     * 메뉴 권한 필터 제외 여부 확인
     * @Author SeungHyeon.Kang
     * @param request
     * @return
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 메뉴관리 API와 코드관리 API만 메뉴 권한 대상으로 판단한다
        return !uri.startsWith(Constant.API_MENUS_PREFIX)
                && !uri.startsWith(Constant.API_CODE_MANAGE_PREFIX);
    }

    /**
     * 메뉴 권한 처리
     * @Author SeungHyeon.Kang
     * @param request
     * @param response
     * @param filterChain
     * @return
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 사이드바 메뉴는 메뉴 목록을 그리기 위한 API이므로 권한 필터 내부 검사를 통과시킨다
        if (Constant.API_MENU_SIDEBAR.equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        AdminSessionVO admin = getAdmin();
        String menuUrlx = getMenuUrlx(request);
        // API 경로에 해당하는 메뉴가 없거나 권한이 없으면 접근 거부로 분기한다
        if (StringUtil.isEmpty(menuUrlx) || !hasPermission(request, menuUrlx, admin)) {
            writeResult(response, HttpServletResponse.SC_FORBIDDEN, ResultData.fail(ResultEnum.FORBIDDEN));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 로그인 관리자 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    private AdminSessionVO getAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 인증 정보가 없거나 Principal 타입이 관리자 세션이 아니면 미인증 상태로 분기한다
        if (StringUtil.isEmpty(authentication) || !(authentication.getPrincipal() instanceof AdminSessionVO admin)) {
            return null;
        }
        return admin;
    }

    /**
     * API 경로 메뉴 URL 변환
     * @Author SeungHyeon.Kang
     * @param request
     * @return
     */
    private String getMenuUrlx(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 메뉴관리 API는 메뉴관리 화면 URL 권한으로 판단한다
        if (uri.startsWith(Constant.API_MENUS_PREFIX)) {
            return Constant.MENU_MANAGE_URL;
        }
        // 코드관리 API는 코드관리 화면 URL 권한으로 판단한다
        if (uri.startsWith(Constant.API_CODE_MANAGE_PREFIX)) {
            return Constant.CODE_MANAGE_URL;
        }
        return null;
    }

    /**
     * HTTP 메서드별 메뉴 권한 확인
     * @Author SeungHyeon.Kang
     * @param request
     * @param menuUrlx
     * @param admin
     * @return
     */
    private boolean hasPermission(HttpServletRequest request, String menuUrlx, AdminSessionVO admin) {
        // 조회 등록 수정 삭제 HTTP 메서드에 따라 메뉴 권한 항목을 분기한다
        return switch (request.getMethod()) {
            case "GET" -> menuPermissionService.hasRead(menuUrlx, admin);
            case "POST", "PUT", "PATCH" -> menuPermissionService.hasWrite(menuUrlx, admin);
            case "DELETE" -> menuPermissionService.hasDelete(menuUrlx, admin);
            default -> false;
        };
    }

    /**
     * 공통 결과 응답 작성
     * @Author SeungHyeon.Kang
     * @param response
     * @param status
     * @param resultData
     * @return
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

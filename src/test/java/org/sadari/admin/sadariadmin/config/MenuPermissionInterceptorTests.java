package org.sadari.admin.sadariadmin.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.menu.mapper.MenuMapper;
import org.sadari.admin.sadariadmin.menu.vo.MenuPermissionVO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 콘텐츠 관리 API가 메뉴 권한 검사에 연결되는지 검증한다. */
class MenuPermissionInterceptorTests {

    /** 메뉴 권한 조회 Mapper */
    private MenuMapper menuMapper;

    /** 테스트 대상 권한 인터셉터 */
    private MenuPermissionInterceptor interceptor;

    /** 제한 관리자 인증을 준비한다. */
    @BeforeEach
    void setUp() {
        menuMapper = mock(MenuMapper.class);
        interceptor = new MenuPermissionInterceptor(menuMapper);
        AdminSessionVO admin = new AdminSessionVO();
        admin.setAdmnNumb(9000912L);
        admin.setAuthCode("QA_LIMIT_0912");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null)
        );
    }

    /** 테스트 뒤 인증 정보를 제거한다. */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /** 콘텐츠 API별 관리자 메뉴 경로를 제공한다. */
    static Stream<Arguments> contentApis() {
        return Stream.of(
                Arguments.of("/api/alim-icons", "/sadari/adm/alimIcon/list"),
                Arguments.of("/api/notices", "/sadari/adm/notice"),
                Arguments.of("/api/service-info", "/sadari/adm/serviceInfo/list"),
                Arguments.of("/api/welcome-pages", "/sadari/adm/welcomePage")
        );
    }

    /** 콘텐츠 API가 대응 메뉴의 조회 권한을 검사하는지 검증한다. */
    @ParameterizedTest
    @MethodSource("contentApis")
    void checksContentPermission(String uri, String menuUrl) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        MenuPermissionVO permission = new MenuPermissionVO();
        permission.setReadYsno("Y");
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getMethod()).thenReturn("GET");
        when(menuMapper.getMenuPermission("QA_LIMIT_0912", menuUrl)).thenReturn(permission);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verify(menuMapper).getMenuPermission("QA_LIMIT_0912", menuUrl);
    }
}
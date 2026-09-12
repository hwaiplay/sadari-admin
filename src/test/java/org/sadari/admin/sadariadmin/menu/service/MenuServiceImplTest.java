package org.sadari.admin.sadariadmin.menu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.menu.mapper.MenuMapper;
import org.sadari.admin.sadariadmin.menu.service.impl.MenuServiceImpl;
import org.sadari.admin.sadariadmin.menu.vo.MenuVO;

/** 관리자 메뉴의 URL 중복과 상하위 데이터 정합성을 검증한다. */
@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private MenuMapper menuMapper;

    private MenuServiceImpl menuService;
    private AdminSessionVO admin;

    /** 각 테스트에서 로그인 관리자와 메뉴 서비스를 구성한다. */
    @BeforeEach
    void setUp() {
        menuService = new MenuServiceImpl(menuMapper);
        admin = new AdminSessionVO();
        admin.setAdmnNumb(1L);
        admin.setAuthCode("SUPER");
    }

    /** 기존 URL을 사용하는 신규 메뉴가 저장되지 않는지 검증한다. */
    @Test
    void rejectsDuplicateMenuUrl() {
        MenuVO menu = new MenuVO();
        menu.setMenuName("QA 메뉴");
        menu.setMenuUrlx(" /sadari/adm/user/list ");
        when(menuMapper.getMenuNumb()).thenReturn("8");
        when(menuMapper.getMenuUrlCount("/sadari/adm/user/list", "8", "0")).thenReturn(1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> menuService.setMenu(menu, admin));

        assertEquals(ResultEnum.COMMON_INVALID_REQUEST, exception.getResultEnum());
        verify(menuMapper, never()).setMenu(menu);
        verify(menuMapper, never()).setMenuAuth(menu);
    }

    /** 하위 메뉴가 있는 상위 메뉴 삭제가 고아 데이터를 만들지 않는지 검증한다. */
    @Test
    void rejectsParentDelete() {
        MenuVO child = new MenuVO();
        child.setMenuNumb("8");
        child.setSubxNumb("1");
        when(menuMapper.getSubMenuList("8")).thenReturn(List.of(child));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> menuService.delMenu("8", "0", admin));

        assertEquals(ResultEnum.COMMON_INVALID_REQUEST, exception.getResultEnum());
        verify(menuMapper, never()).delMenuAuth("8", "0");
        verify(menuMapper, never()).delMenu("8", "0");
    }
}

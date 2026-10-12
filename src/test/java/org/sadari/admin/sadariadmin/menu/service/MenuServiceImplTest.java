package org.sadari.admin.sadariadmin.menu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.sadari.admin.sadariadmin.menu.vo.MenuSaveVO;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : MenuServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 관리자 메뉴의 URL 중복과 상하위 데이터 정합성을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 * 2026-10-12        SeungHyeon.Kang    메뉴 상세 일괄 저장 검증 추가
 */
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

    /** 메뉴 상세 전체 변경이 하나의 트랜잭션 진입점에서 저장되는지 검증한다. */
    @Test
    void savesMenuBatchInTx() throws NoSuchMethodException {
        // 수정할 상위 메뉴를 준비한다
        MenuVO parent = createMenu("8", "0", "/admin/menu");
        // 수정할 기존 하위 메뉴를 준비한다
        MenuVO subMenu = createMenu("8", "1", "/admin/menu/sub");
        // 등록할 신규 하위 메뉴를 준비한다
        MenuVO childMenu = createMenu(null, null, "/admin/menu/new");
        // 한 요청으로 저장할 메뉴 상세 데이터를 구성한다
        MenuSaveVO save = new MenuSaveVO();
        // 기준 메뉴를 일괄 저장 데이터에 설정한다
        save.setMenu(parent);
        // 기존 하위 메뉴를 일괄 저장 데이터에 설정한다
        save.setSubMenus(List.of(subMenu));
        // 신규 하위 메뉴를 일괄 저장 데이터에 설정한다
        save.setChildMenus(List.of(childMenu));
        // 신규 하위 메뉴 번호와 저장 후 조회 결과를 설정한다
        when(menuMapper.getSubxNumb("8")).thenReturn("2");
        // 기준 메뉴 저장 후 조회 결과를 설정한다
        when(menuMapper.getMenuDtl("8", "0")).thenReturn(parent);
        // 기존 하위 메뉴 저장 후 조회 결과를 설정한다
        when(menuMapper.getMenuDtl("8", "1")).thenReturn(subMenu);
        // 신규 하위 메뉴 저장 후 조회 결과를 설정한다
        when(menuMapper.getMenuDtl("8", "2")).thenReturn(childMenu);

        // 메뉴 상세 전체 저장을 실행한다
        MenuVO saved = menuService.setMenuBatch(save, admin);

        // 기준 메뉴 저장 결과가 반환되는지 확인한다
        assertEquals(parent, saved);
        // 기준 메뉴와 기존 하위 메뉴가 수정되는지 확인한다
        verify(menuMapper).uptMenu(parent);
        verify(menuMapper).uptMenu(subMenu);
        // 신규 하위 메뉴가 등록되는지 확인한다
        verify(menuMapper).setMenu(childMenu);
        // 전체 저장 진입점에 트랜잭션 경계가 선언됐는지 확인한다
        assertTrue(MenuServiceImpl.class.getMethod("setMenuBatch", MenuSaveVO.class, AdminSessionVO.class)
                .isAnnotationPresent(Transactional.class));
    }

    /** 일괄 저장 테스트에 사용할 메뉴 입력값을 생성한다. */
    private MenuVO createMenu(String menuNumb, String subxNumb, String menuUrlx) {
        // 메뉴 저장 필수값을 담을 객체를 생성한다
        MenuVO menu = new MenuVO();
        // 기존 메뉴와 신규 메뉴를 구분할 상위 번호를 설정한다
        menu.setMenuNumb(menuNumb);
        // 기존 메뉴와 신규 메뉴를 구분할 하위 번호를 설정한다
        menu.setSubxNumb(subxNumb);
        // 저장 검증을 통과할 메뉴명을 설정한다
        menu.setMenuName("테스트 메뉴");
        // 중복 검증에 사용할 메뉴 URL을 설정한다
        menu.setMenuUrlx(menuUrlx);
        // 완성된 메뉴 입력값을 반환한다
        return menu;
    }
}

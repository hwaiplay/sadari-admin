package org.sadari.admin.sadariadmin.usermenu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.usermenu.mapper.UserMenuMapper;
import org.sadari.admin.sadariadmin.usermenu.service.impl.UserMenuServiceImpl;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuVO;

/**
 * fileName       : UserMenuServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 사용자 메뉴 3단계 계층 검증을 확인한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class UserMenuServiceImplTest {

    /** 사용자 메뉴 Mapper 테스트 대역 */
    @Mock
    private UserMenuMapper userMenuMapper;

    /** 사용자 메뉴 서비스 테스트 대상 */
    private UserMenuServiceImpl userMenuService;

    /** 로그인 관리자 세션 */
    private AdminSessionVO admin;

    /** 각 테스트의 사용자 메뉴 서비스와 관리자 세션을 생성한다. */
    @BeforeEach
    void setUp() {
        // 사용자 메뉴 서비스 테스트 대상을 생성한다
        userMenuService = new UserMenuServiceImpl(userMenuMapper);
        // 로그인 관리자 세션을 생성한다
        admin = new AdminSessionVO();
        // 테스트 관리자 번호를 설정한다
        admin.setAdmnNumb(1L);
    }

    /** 2단계 메뉴 아래에 신규 메뉴를 등록하면 3단계로 저장하는지 확인한다. */
    @Test
    void setUserMenuCreatesThirdLevelMenu() {
        // 2단계 상위 사용자 메뉴를 생성한다
        UserMenuVO parentMenu = getMenu(20L, 2);
        // 상위 메뉴 번호를 사용한 신규 사용자 메뉴를 생성한다
        UserMenuVO newMenu = getMenu(null, null);
        // 신규 메뉴의 상위 메뉴 번호를 설정한다
        newMenu.setParnNumb(20L);
        // 선택한 상위 메뉴 상세 조회 결과를 설정한다
        when(userMenuMapper.getUserMenuDtl(20L)).thenReturn(parentMenu);
        // 등록 시 데이터베이스가 생성한 메뉴 번호를 저장 대상에 반영한다
        doAnswer(invocation -> {
            // 등록할 사용자 메뉴 인자를 조회한다
            UserMenuVO menu = invocation.getArgument(0);
            // 데이터베이스 생성 메뉴 번호를 설정한다
            menu.setMenuNumb(30L);
            // 반환값이 없는 Mapper 호출을 종료한다
            return null;
        }).when(userMenuMapper).setUserMenu(newMenu);
        // 등록 뒤 조회할 사용자 메뉴 상세를 설정한다
        when(userMenuMapper.getUserMenuDtl(30L)).thenReturn(newMenu);

        // 신규 사용자 메뉴 등록 업무를 실행한다
        userMenuService.setUserMenu(newMenu, admin);

        // Mapper에 전달된 저장 사용자 메뉴를 확인한다
        ArgumentCaptor<UserMenuVO> menuCaptor = ArgumentCaptor.forClass(UserMenuVO.class);
        // 사용자 메뉴 등록 Mapper가 호출됐는지 검증한다
        verify(userMenuMapper).setUserMenu(menuCaptor.capture());
        // 2단계 상위 메뉴 아래 신규 메뉴 단계가 3인지 검증한다
        assertEquals(3, menuCaptor.getValue().getMenuLevl());
    }

    /** 3단계 메뉴 아래에 신규 메뉴를 등록하지 못하는지 확인한다. */
    @Test
    void setUserMenuRejectsFourthLevelMenu() {
        // 3단계 상위 사용자 메뉴를 생성한다
        UserMenuVO parentMenu = getMenu(30L, 3);
        // 3단계 메뉴 아래에 등록할 신규 사용자 메뉴를 생성한다
        UserMenuVO newMenu = getMenu(null, null);
        // 신규 메뉴의 상위 메뉴 번호를 설정한다
        newMenu.setParnNumb(30L);
        // 선택한 상위 메뉴 상세 조회 결과를 설정한다
        when(userMenuMapper.getUserMenuDtl(30L)).thenReturn(parentMenu);

        // 4단계가 되는 사용자 메뉴 등록을 업무 오류로 검증한다
        assertThrows(BusinessException.class, () -> userMenuService.setUserMenu(newMenu, admin));
        // 검증에 실패한 사용자 메뉴가 등록되지 않았는지 확인한다
        verify(userMenuMapper, never()).setUserMenu(newMenu);
    }

    /** 하위 메뉴가 있는 사용자 메뉴를 삭제하지 못하는지 확인한다. */
    @Test
    void delUserMenuRejectsMenuWithChildren() {
        // 삭제 대상 사용자 메뉴를 생성한다
        UserMenuVO savedMenu = getMenu(10L, 1);
        // 삭제 대상 사용자 메뉴 상세 조회 결과를 설정한다
        when(userMenuMapper.getUserMenuDtl(10L)).thenReturn(savedMenu);
        // 삭제 대상의 하위 사용자 메뉴 건수를 설정한다
        when(userMenuMapper.getUserMenuChildCount(10L)).thenReturn(1);

        // 하위 메뉴가 있는 사용자 메뉴 삭제를 업무 오류로 검증한다
        assertThrows(BusinessException.class, () -> userMenuService.delUserMenu(10L, admin));
        // 하위 메뉴가 있는 사용자 메뉴가 삭제되지 않았는지 확인한다
        verify(userMenuMapper, never()).delUserMenu(10L);
    }

    /** 사용자 메뉴 테스트 데이터를 생성한다. */
    private UserMenuVO getMenu(Long menuNumb, Integer menuLevl) {
        // 사용자 메뉴 테스트 데이터를 생성한다
        UserMenuVO menu = new UserMenuVO();
        // 테스트 메뉴 번호를 설정한다
        menu.setMenuNumb(menuNumb);
        // 테스트 메뉴 단계를 설정한다
        menu.setMenuLevl(menuLevl);
        // 필수 메뉴명을 설정한다
        menu.setMenuName("테스트 메뉴");
        // 그룹 메뉴도 허용되도록 빈 URL을 설정한다
        menu.setMenuUrlx("");
        // 생성한 사용자 메뉴 테스트 데이터를 반환한다
        return menu;
    }
}

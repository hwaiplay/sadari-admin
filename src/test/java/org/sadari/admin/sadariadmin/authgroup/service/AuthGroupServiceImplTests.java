package org.sadari.admin.sadariadmin.authgroup.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.authgroup.mapper.AuthGroupMapper;
import org.sadari.admin.sadariadmin.authgroup.service.impl.AuthGroupServiceImpl;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifies authorization-group deletion conditions. */
class AuthGroupServiceImplTests {

    /** Authorization-group data mapper. */
    private AuthGroupMapper authGroupMapper;

    /** Service under test. */
    private AuthGroupServiceImpl service;

    /** Prepares test dependencies. */
    @BeforeEach
    void setUp() {
        authGroupMapper = mock(AuthGroupMapper.class);
        service = new AuthGroupServiceImpl(authGroupMapper);
    }

    /** Rejects deletion when an administrator uses the group. */
    @Test
    void blocksUsedGroupDelete() {
        when(authGroupMapper.getAuthGroupCount("QA_LIMIT_0912")).thenReturn(1);
        when(authGroupMapper.getAuthGroupAdminCount("QA_LIMIT_0912")).thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.delAuthGroup("QA_LIMIT_0912", admin())
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(authGroupMapper, never()).delAuthMenu("QA_LIMIT_0912");
        verify(authGroupMapper, never()).delAuthGroup("QA_LIMIT_0912");
    }

    /** Deletes menus and the group when no administrator uses it. */
    @Test
    void deletesUnusedAuthGroup() {
        when(authGroupMapper.getAuthGroupCount("QA_UNUSED_0912")).thenReturn(1);
        when(authGroupMapper.getAuthGroupAdminCount("QA_UNUSED_0912")).thenReturn(0);

        service.delAuthGroup("QA_UNUSED_0912", admin());

        verify(authGroupMapper).delAuthMenu("QA_UNUSED_0912");
        verify(authGroupMapper).delAuthGroup("QA_UNUSED_0912");
    }

    /** Creates an authenticated administrator session. */
    private AdminSessionVO admin() {
        AdminSessionVO admin = new AdminSessionVO();
        admin.setAdmnNumb(1L);
        return admin;
    }
}

package org.sadari.admin.sadariadmin.admin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sadari.admin.sadariadmin.admin.mapper.AdminMapper;
import org.sadari.admin.sadariadmin.admin.service.impl.AdminAuthServiceImpl;
import org.sadari.admin.sadariadmin.admin.vo.AdminLoginRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.admin.vo.AdminVO;
import org.sadari.admin.sadariadmin.common.PasswordHash;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 관리자 로그인 실패 제한을 검증한다. */
class AdminAuthServiceImplTests {

    /** 관리자 데이터 접근 Mapper */
    private AdminMapper adminMapper;

    /** Redis 문자열 값 접근 도구 */
    private ValueOperations<String, String> valueOperations;

    /** Redis 접근 도구 */
    private StringRedisTemplate redisTemplate;

    /** 테스트 대상 관리자 인증 서비스 */
    private AdminAuthServiceImpl service;

    /** 테스트 의존성을 준비한다. */
    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        adminMapper = mock(AdminMapper.class);
        valueOperations = mock(ValueOperations.class);
        redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new AdminAuthServiceImpl(adminMapper, redisTemplate);
    }

    /** 실패 한도에 도달한 로그인을 차단하는지 검증한다. */
    @Test
    void blocksAtFailureLimit() {
        when(valueOperations.get(anyString())).thenReturn("5");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.setAdminLogin(request("wrong"))
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatus());
        verify(adminMapper, never()).getAdminDtl(anyString());
    }

    /** 잘못된 자격증명의 실패 횟수를 기록하는지 검증한다. */
    @Test
    void recordsInvalidLogin() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.setAdminLogin(request("wrong"))
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(valueOperations).increment(anyString());
        verify(redisTemplate).expire(anyString(), eq(Duration.ofSeconds(900)));
    }

    /** 정상 로그인 후 실패 제한을 제거하는지 검증한다. */
    @Test
    void clearsLimitOnSuccess() {
        AdminVO admin = new AdminVO();
        admin.setAdmnNumb(9000912L);
        admin.setAdmnIdxx("qa_limit_0912");
        admin.setAdmnName("QA 제한관리자");
        admin.setPassWord(PasswordHash.sha256("valid"));
        admin.setAuthCode("ADMIN");
        admin.setAuthLevel(1);
        when(adminMapper.getAdminDtl("qa_limit_0912")).thenReturn(admin);

        AdminSessionVO session = service.setAdminLogin(request("valid"));

        assertEquals(9000912L, session.getAdmnNumb());
        verify(redisTemplate).delete(anyString());
        verify(adminMapper).uptAdminLoginSuccess(9000912L);
    }

    /** 지정 비밀번호를 가진 로그인 요청을 생성한다. */
    private AdminLoginRequest request(String password) {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setAdmnIdxx("qa_limit_0912");
        request.setPassWord(password);
        return request;
    }
}
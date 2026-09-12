package org.sadari.admin.sadariadmin.admin.service.impl;

import org.sadari.admin.sadariadmin.admin.mapper.AdminMapper;
import org.sadari.admin.sadariadmin.admin.service.AdminAuthService;
import org.sadari.admin.sadariadmin.admin.vo.AdminLoginRequest;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.admin.vo.AdminVO;
import org.sadari.admin.sadariadmin.common.PasswordHash;
import org.sadari.admin.sadariadmin.common.constant.AuthConstant;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Locale;

/**
 * fileName       : AdminAuthServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 관리자 인증 서비스 구현체 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 */
@Service
@Transactional(readOnly = true)
public class AdminAuthServiceImpl implements AdminAuthService {

    /** 관리자 데이터 접근 Mapper */
    private final AdminMapper adminMapper;

    /** 로그인 실패 횟수 저장 Redis 도구 */
    private final StringRedisTemplate redisTemplate;

    /**
     * 관리자 인증 서비스 생성
     * @author SeungHyeon.Kang
     * @param adminMapper 관리자 데이터 접근 Mapper
     * @param redisTemplate 로그인 실패 횟수 저장 Redis 도구
     * @return
     */
    public AdminAuthServiceImpl(AdminMapper adminMapper, StringRedisTemplate redisTemplate) {
        this.adminMapper = adminMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 관리자 로그인 처리
     * @author SeungHyeon.Kang
     * @param request 로그인 요청
     * @return 관리자 세션 정보
     */
    @Override
    @Transactional
    public AdminSessionVO setAdminLogin(AdminLoginRequest request) {
        if (StringUtil.isEmpty(request.getAdmnIdxx()) || StringUtil.isEmpty(request.getPassWord())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.AUTH_INVALID_REQUEST);
        }

        String loginId = request.getAdmnIdxx().trim().toLowerCase(Locale.ROOT);
        String attemptKey = getAttemptKey(loginId);
        checkAttemptLimit(attemptKey);

        AdminVO admin = adminMapper.getAdminDtl(request.getAdmnIdxx());
        if (StringUtil.isEmpty(admin)
                || !PasswordHash.sha256(request.getPassWord()).equalsIgnoreCase(admin.getPassWord())) {
            recordLoginFailure(attemptKey);
            if (!StringUtil.isEmpty(admin)) {
                adminMapper.uptAdminLoginFail(admin.getAdmnNumb());
            }
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_INVALID_CREDENTIALS);
        }

        if (StringUtil.isEmpty(admin.getAuthLevel())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, ResultEnum.AUTH_INVALID_CODE);
        }

        redisTemplate.delete(attemptKey);
        adminMapper.uptAdminLoginSuccess(admin.getAdmnNumb());

        AdminSessionVO session = new AdminSessionVO();
        session.setAdmnNumb(admin.getAdmnNumb());
        session.setAdmnIdxx(admin.getAdmnIdxx());
        session.setAdmnName(admin.getAdmnName());
        session.setAuthCode(admin.getAuthCode());
        session.setAuthLevel(admin.getAuthLevel());
        session.setDeptCode(admin.getDeptCode());
        return session;
    }

    /** 로그인 실패 제한을 초과한 요청을 차단한다. */
    private void checkAttemptLimit(String attemptKey) {
        String count = redisTemplate.opsForValue().get(attemptKey);
        if (!StringUtil.isEmpty(count) && Integer.parseInt(count) >= AuthConstant.LOGIN_MAX_FAILURES) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, ResultEnum.AUTH_INVALID_CREDENTIALS);
        }
    }

    /** 로그인 실패 횟수를 제한 시간 동안 누적한다. */
    private void recordLoginFailure(String attemptKey) {
        Long count = redisTemplate.opsForValue().increment(attemptKey);
        if (Long.valueOf(1L).equals(count)) {
            redisTemplate.expire(attemptKey, Duration.ofSeconds(AuthConstant.LOGIN_FAILURE_WINDOW_SECONDS));
        }
    }

    /** 관리자 ID 원문을 노출하지 않는 로그인 실패 키를 생성한다. */
    private String getAttemptKey(String loginId) {
        return AuthConstant.LOGIN_ATTEMPT_KEY_PREFIX + ":" + PasswordHash.sha256(loginId);
    }
}

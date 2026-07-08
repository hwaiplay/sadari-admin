package org.sadari.admin.sadariadmin.admin.service.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.sadari.admin.sadariadmin.admin.service.AdminRedisAuthService;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.config.AuthRedisProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Redis에 관리자 인증 정보를 저장하고 조회한다.
 */
@Service
public class AdminRedisAuthServiceImpl implements AdminRedisAuthService {

    /** Redis 저장 필드: 관리자 번호. */
    private static final String ADMN_NUMB = "ADMN_NUMB";

    /** Redis 저장 필드: 권한 코드. */
    private static final String AUTH_ROLE = "AUTH_ROLE";

    /** Redis 저장 필드: 권한 레벨. */
    private static final String AUTH_LEVEL = "AUTH_LEVEL";

    /** Redis 저장 필드: 관리자 아이디. */
    private static final String ADMN_IDXX = "ADMN_IDXX";

    /** Redis 저장 필드: 관리자 이름. */
    private static final String ADMN_NAME = "ADMN_NAME";

    /** Redis 저장 필드: 부서 코드. */
    private static final String DEPT_CODE = "DEPT_CODE";

    /** 문자열 Redis 접근 도구. */
    private final StringRedisTemplate redisTemplate;

    /** Redis 인증 설정. */
    private final AuthRedisProperties properties;

    public AdminRedisAuthServiceImpl(StringRedisTemplate redisTemplate, AuthRedisProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /**
     * 관리자 권한 정보를 Redis Hash로 저장한다.
     */
    @Override
    public String setAdminToken(AdminSessionVO admin) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = getRedisKey(token);

        redisTemplate.opsForHash().putAll(key, Map.of(
                ADMN_NUMB, String.valueOf(admin.getAdmnNumb()),
                AUTH_ROLE, admin.getAuthCode(),
                AUTH_LEVEL, String.valueOf(admin.getAuthLevel()),
                ADMN_IDXX, admin.getAdmnIdxx(),
                ADMN_NAME, admin.getAdmnName(),
                DEPT_CODE, admin.getDeptCode() == null ? "" : admin.getDeptCode()
        ));
        redisTemplate.expire(key, Duration.ofSeconds(properties.getTokenTtlSeconds()));
        return token;
    }

    /**
     * 요청 쿠키에서 토큰을 꺼내 Redis 관리자 정보를 조회한다.
     */
    @Override
    public AdminSessionVO getAdminSessionDtl(HttpServletRequest request) {
        String token = getToken(request);
        if (token == null) {
            return null;
        }

        String key = getRedisKey(token);
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return null;
        }

        Object admnNumb = redisTemplate.opsForHash().get(key, ADMN_NUMB);
        Object authRole = redisTemplate.opsForHash().get(key, AUTH_ROLE);
        Object authLevel = redisTemplate.opsForHash().get(key, AUTH_LEVEL);
        if (admnNumb == null || authRole == null || authLevel == null) {
            return null;
        }

        AdminSessionVO admin = new AdminSessionVO();
        admin.setAdmnNumb(Long.valueOf(String.valueOf(admnNumb)));
        admin.setAuthCode(String.valueOf(authRole));
        admin.setAuthLevel(Integer.valueOf(String.valueOf(authLevel)));
        admin.setAdmnIdxx(getHashValue(key, ADMN_IDXX));
        admin.setAdmnName(getHashValue(key, ADMN_NAME));
        admin.setDeptCode(getHashValue(key, DEPT_CODE));
        redisTemplate.expire(key, Duration.ofSeconds(properties.getTokenTtlSeconds()));
        return admin;
    }

    /**
     * 요청 쿠키의 토큰에 해당하는 Redis 키를 삭제한다.
     */
    @Override
    public void delAdminToken(HttpServletRequest request) {
        String token = getToken(request);
        if (token != null) {
            redisTemplate.delete(getRedisKey(token));
        }
    }

    /**
     * Redis 로그인 키를 만든다.
     */
    private String getRedisKey(String token) {
        return properties.getRedisKeyPrefix() + ":" + token;
    }

    /**
     * Redis Hash 값을 문자열로 조회한다.
     */
    private String getHashValue(String key, String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 요청 쿠키에서 관리자 인증 토큰을 찾는다.
     */
    private String getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (properties.getCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

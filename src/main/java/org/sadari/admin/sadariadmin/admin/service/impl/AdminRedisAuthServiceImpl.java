package org.sadari.admin.sadariadmin.admin.service.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.sadari.admin.sadariadmin.admin.service.AdminRedisAuthService;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.AuthConstant;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.config.AuthRedisProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Redis 관리자 인증 정보 서비스 구현체
 */
@Service
public class AdminRedisAuthServiceImpl implements AdminRedisAuthService {

    /** 문자열 Redis 접근 도구 */
    private final StringRedisTemplate redisTemplate;

    /** Redis 인증 설정 */
    private final AuthRedisProperties properties;

    /**
     * Redis 관리자 인증 정보 서비스 생성
     * @Author SeungHyeon.Kang
     * @param redisTemplate
     * @param properties
     * @return
     */
    public AdminRedisAuthServiceImpl(StringRedisTemplate redisTemplate, AuthRedisProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /**
     * 관리자 인증 토큰 저장
     * @Author SeungHyeon.Kang
     * @param admin
     * @return
     */
    @Override
    public String setAdminToken(AdminSessionVO admin) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = getRedisKey(token);

        Map<String, String> authMap = new HashMap<>();
        authMap.put(AuthConstant.REDIS_ADMN_NUMB, toRedisValue(admin.getAdmnNumb()));
        authMap.put(AuthConstant.REDIS_AUTH_ROLE, toRedisValue(admin.getAuthCode()));
        authMap.put(AuthConstant.REDIS_AUTH_LEVEL, toRedisValue(admin.getAuthLevel()));
        authMap.put(AuthConstant.REDIS_ADMN_IDXX, toRedisValue(admin.getAdmnIdxx()));
        authMap.put(AuthConstant.REDIS_ADMN_NAME, toRedisValue(admin.getAdmnName()));
        authMap.put(AuthConstant.REDIS_DEPT_CODE, toRedisValue(admin.getDeptCode()));

        redisTemplate.opsForHash().putAll(key, authMap);
        redisTemplate.expire(key, Duration.ofSeconds(properties.getTokenTtlSeconds()));
        return token;
    }

    /**
     * 관리자 세션 조회
     * @Author SeungHyeon.Kang
     * @param request
     * @return
     */
    @Override
    public AdminSessionVO getAdminSessionDtl(HttpServletRequest request) {
        String token = getToken(request);
        // 쿠키에서 토큰을 찾지 못하면 미인증 상태로 분기한다
        if (StringUtil.isEmpty(token)) {
            return null;
        }

        String key = getRedisKey(token);
        // Redis에 토큰 키가 없으면 만료되었거나 로그아웃된 상태로 분기한다
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return null;
        }

        Object admnNumb = redisTemplate.opsForHash().get(key, AuthConstant.REDIS_ADMN_NUMB);
        Object authRole = redisTemplate.opsForHash().get(key, AuthConstant.REDIS_AUTH_ROLE);
        Object authLevel = redisTemplate.opsForHash().get(key, AuthConstant.REDIS_AUTH_LEVEL);
        // 인증에 필요한 필수 Hash 값이 없으면 잘못된 토큰 상태로 분기한다
        if (StringUtil.isEmpty(admnNumb) || StringUtil.isEmpty(authRole) || StringUtil.isEmpty(authLevel)) {
            return null;
        }

        AdminSessionVO admin = new AdminSessionVO();
        admin.setAdmnNumb(Long.valueOf(String.valueOf(admnNumb)));
        admin.setAuthCode(String.valueOf(authRole));
        admin.setAuthLevel(Integer.valueOf(String.valueOf(authLevel)));
        admin.setAdmnIdxx(getHashValue(key, AuthConstant.REDIS_ADMN_IDXX));
        admin.setAdmnName(getHashValue(key, AuthConstant.REDIS_ADMN_NAME));
        admin.setDeptCode(getHashValue(key, AuthConstant.REDIS_DEPT_CODE));
        redisTemplate.expire(key, Duration.ofSeconds(properties.getTokenTtlSeconds()));
        return admin;
    }

    /**
     * 관리자 인증 토큰 삭제
     * @Author SeungHyeon.Kang
     * @param request
     * @return
     */
    @Override
    public void delAdminToken(HttpServletRequest request) {
        String token = getToken(request);
        // 요청 쿠키에 토큰이 있을 때만 Redis 키를 삭제한다
        if (!StringUtil.isEmpty(token)) {
            redisTemplate.delete(getRedisKey(token));
        }
    }

    /**
     * Redis 로그인 키 생성
     * @Author SeungHyeon.Kang
     * @param token
     * @return
     */
    private String getRedisKey(String token) {
        return properties.getRedisKeyPrefix() + ":" + token;
    }

    /**
     * Redis Hash 값 조회
     * @Author SeungHyeon.Kang
     * @param key
     * @param field
     * @return
     */
    private String getHashValue(String key, String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        // Redis Hash 필드가 없으면 null로 반환한다
        return StringUtil.isEmpty(value) ? null : String.valueOf(value);
    }

    /**
     * Redis 저장 값 변환
     * @Author SeungHyeon.Kang
     * @param value
     * @return
     */
    private String toRedisValue(Object value) {
        // Redis Hash는 null 값을 저장할 수 없으므로 빈 문자열로 변환한다
        return StringUtil.isEmpty(value) ? "" : String.valueOf(value);
    }

    /**
     * 요청 쿠키 인증 토큰 조회
     * @Author SeungHyeon.Kang
     * @param request
     * @return
     */
    private String getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        // 요청에 쿠키가 없으면 토큰 없음으로 분기한다
        if (StringUtil.isEmpty(cookies)) {
            return null;
        }

        for (Cookie cookie : cookies) {
            // 설정된 인증 쿠키명과 일치하는 쿠키 값을 토큰으로 사용한다
            if (properties.getCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

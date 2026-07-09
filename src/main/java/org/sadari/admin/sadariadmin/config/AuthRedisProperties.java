package org.sadari.admin.sadariadmin.config;

import org.sadari.admin.sadariadmin.common.constant.AuthConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 관리자 인증 설정
 */
@ConfigurationProperties(prefix = "sadari.auth")
public class AuthRedisProperties {

    /** Redis 로그인 키 접두어 */
    private String redisKeyPrefix = AuthConstant.REDIS_KEY_PREFIX;

    /** 관리자 인증 토큰 쿠키명 */
    private String cookieName = AuthConstant.COOKIE_NAME;

    /** Redis 토큰 만료 시간 */
    private long tokenTtlSeconds = AuthConstant.TOKEN_TTL_SECONDS;

    /**
     * Redis 로그인 키 접두어 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    /**
     * Redis 로그인 키 접두어 설정
     * @Author SeungHyeon.Kang
     * @param redisKeyPrefix
     * @return
     */
    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    /**
     * 인증 쿠키명 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    public String getCookieName() {
        return cookieName;
    }

    /**
     * 인증 쿠키명 설정
     * @Author SeungHyeon.Kang
     * @param cookieName
     * @return
     */
    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    /**
     * 토큰 만료 시간 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    public long getTokenTtlSeconds() {
        return tokenTtlSeconds;
    }

    /**
     * 토큰 만료 시간 설정
     * @Author SeungHyeon.Kang
     * @param tokenTtlSeconds
     * @return
     */
    public void setTokenTtlSeconds(long tokenTtlSeconds) {
        this.tokenTtlSeconds = tokenTtlSeconds;
    }
}

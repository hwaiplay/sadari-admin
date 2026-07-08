package org.sadari.admin.sadariadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 관리자 인증 설정.
 */
@ConfigurationProperties(prefix = "sadari.auth")
public class AuthRedisProperties {

    /** Redis 로그인 키 접두어. */
    private String redisKeyPrefix = "sadari:adm:login";

    /** 관리자 인증 토큰 쿠키명. */
    private String cookieName = "SADARI_ADM_TOKEN";

    /** Redis 토큰 만료 시간. */
    private long tokenTtlSeconds = 7200;

    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public long getTokenTtlSeconds() {
        return tokenTtlSeconds;
    }

    public void setTokenTtlSeconds(long tokenTtlSeconds) {
        this.tokenTtlSeconds = tokenTtlSeconds;
    }
}

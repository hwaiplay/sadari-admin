package org.sadari.admin.sadariadmin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 관리자 인증 설정 활성화
 */
@Configuration
@EnableConfigurationProperties(AuthRedisProperties.class)
public class AuthRedisConfig {
}

package org.sadari.admin.sadariadmin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * fileName       : AuthRedisConfig
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : Redis 관리자 인증 설정 활성화 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 */
@Configuration
@EnableConfigurationProperties(AuthRedisProperties.class)
public class AuthRedisConfig {
}

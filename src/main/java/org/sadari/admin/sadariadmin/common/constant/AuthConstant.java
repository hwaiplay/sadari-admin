package org.sadari.admin.sadariadmin.common.constant;

/**
 * 인증과 권한 공통 상수
 */
public final class AuthConstant {

    /** 권한 공통코드 */
    public static final String AUTH_CODE = "AUTH_CODE";

    /** Spring Security 권한 접두어 */
    public static final String ROLE_PREFIX = "ROLE_";

    /** 최고 관리자 권한 코드 */
    public static final String SUPER_AUTH_CODE = "SUPER";

    /** 관리자 Redis 키 접두어 */
    public static final String REDIS_KEY_PREFIX = "sadari:adm:login";

    /** 관리자 인증 쿠키명 */
    public static final String COOKIE_NAME = "SADARI_ADM_TOKEN";

    /** 관리자 인증 토큰 만료 시간 */
    public static final long TOKEN_TTL_SECONDS = 7200;

    /** 로그아웃 응답 빈 토큰 값 */
    public static final String EMPTY_TOKEN = "";

    /** 로그아웃 쿠키 만료 Max Age 값 */
    public static final int COOKIE_DELETE_MAX_AGE = 0;

    /** Redis 저장 필드 관리자 번호 */
    public static final String REDIS_ADMN_NUMB = "ADMN_NUMB";

    /** Redis 저장 필드 권한 코드 */
    public static final String REDIS_AUTH_ROLE = "AUTH_ROLE";

    /** Redis 저장 필드 권한 레벨 */
    public static final String REDIS_AUTH_LEVEL = "AUTH_LEVEL";

    /** Redis 저장 필드 관리자 아이디 */
    public static final String REDIS_ADMN_IDXX = "ADMN_IDXX";

    /** Redis 저장 필드 관리자 이름 */
    public static final String REDIS_ADMN_NAME = "ADMN_NAME";

    /** Redis 저장 필드 부서 코드 */
    public static final String REDIS_DEPT_CODE = "DEPT_CODE";

    /**
     * 인증 상수 생성 방지
     * @Author SeungHyeon.Kang
     * @return
     */
    private AuthConstant() {
    }
}

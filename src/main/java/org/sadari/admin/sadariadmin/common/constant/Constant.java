package org.sadari.admin.sadariadmin.common.constant;

/**
 * 프로젝트 전역 공통 상수
 */
public final class Constant {

    /** 성공 코드 */
    public static final int SUCCESS_CODE = 200;

    /** 성공 메시지 */
    public static final String SUCCESS_MESSAGE = "success";

    /** 사용 여부 사용 */
    public static final String YES = "Y";

    /** 사용 여부 미사용 */
    public static final String NO = "N";

    /** 사용 여부 공통코드 */
    public static final String COMM_YSNO = "COMM_YSNO";

    /** 사용 여부 옵션 코드 */
    public static final String USEE_YSNO_CODE = "USEE_YSNO";

    /** 알림 상황 공통코드 */
    public static final String ALIM_SITU = "ALIM_SITU";

    /** 상위 메뉴 SUBX NUMB 값 */
    public static final String TOP_MENU_SUBX_NUMB = "0";

    /** 메뉴 기본 정렬 순서 */
    public static final int DEFAULT_MENU_SORT_ORDR = 10;

    /** 관리자 URL 접두어 */
    public static final String ADMIN_URL_PREFIX = "/sadari/adm";

    /** 메뉴관리 화면 URL */
    public static final String MENU_MANAGE_URL = "/sadari/adm/menu/list";

    /** 코드관리 화면 URL */
    public static final String CODE_MANAGE_URL = "/sadari/adm/code/list";

    /** 알림 템플릿 관리 화면 URL */
    public static final String ALIM_TEMP_MANAGE_URL = "/sadari/adm/alimTemp/list";

    /** 로그인 API URL */
    public static final String API_AUTH_LOGIN = "/api/auth/login";

    /** 인증 API URL 접두어 */
    public static final String API_AUTH_PREFIX = "/api/auth";

    /** 코드 읽기 API URL 접두어 */
    public static final String API_CODES_PREFIX = "/api/codes";

    /** 코드 읽기 API URL 패턴 */
    public static final String API_CODES_PATTERN = "/api/codes/**";

    /** 로그아웃 API URL */
    public static final String API_AUTH_LOGOUT = "/api/auth/logout";

    /** 로그인 사용자 조회 API URL */
    public static final String API_AUTH_ME = "/api/auth/me";

    /** 메뉴 권한 조회 API URL 패턴 */
    public static final String API_MENU_PERMISSIONS_PATTERN = "/api/menu-permissions/**";

    /** 메뉴 권한 조회 API URL 접두어 */
    public static final String API_MENU_PERMISSIONS_PREFIX = "/api/menu-permissions";

    /** 사이드바 메뉴 API URL */
    public static final String API_MENU_SIDEBAR = "/api/menus/sidebar";

    /** 메뉴 API URL 패턴 */
    public static final String API_MENUS_PATTERN = "/api/menus/**";

    /** 코드관리 API URL 패턴 */
    public static final String API_CODE_MANAGE_PATTERN = "/api/code-manage/**";

    /** 알림 템플릿 관리 API URL 패턴 */
    public static final String API_ALIM_TEMP_PATTERN = "/api/alim-temps/**";

    /** 직원 API URL 패턴 */
    public static final String API_EMPLOYEES_PATTERN = "/api/employees/**";

    /** 직원 API URL 접두어 */
    public static final String API_EMPLOYEES_PREFIX = "/api/employees";

    /** 메뉴 API URL 접두어 */
    public static final String API_MENUS_PREFIX = "/api/menus";

    /** 코드관리 API URL 접두어 */
    public static final String API_CODE_MANAGE_PREFIX = "/api/code-manage";

    /** 알림 템플릿 관리 API URL 접두어 */
    public static final String API_ALIM_TEMP_PREFIX = "/api/alim-temps";

    /**
     * 공통 상수 생성 방지
     * @Author SeungHyeon.Kang
     * @return
     */
    private Constant() {
    }
}

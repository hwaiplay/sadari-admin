package org.sadari.admin.sadariadmin.common.constant;

/**
 * fileName       : Constant
 * author         : SeungHyeon.Kang
 * date           : 2026-07-09
 * description    : 프로젝트 전역 공통 상수 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-09        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    스케줄러 코드 공통코드 상수 추가
 * 2026-07-30        SeungHyeon.Kang    팝업 콘텐츠 관리 상수 추가
 * 2026-07-30        SeungHyeon.Kang    현재 사용자 조회 관리 상수 추가
 * 2026-07-30        SeungHyeon.Kang    회원 이용 정지 관리 상수 추가
 * 2026-07-30        SeungHyeon.Kang    회원 상태 변경 Outbox 이벤트 상수 추가
 * 2026-07-30        SeungHyeon.Kang    사용자 서버 상태 반영 결과 상수 추가
 * 2026-07-30        SeungHyeon.Kang    로그인 제공자 공통코드 상수 추가
 * 2026-08-05        SeungHyeon.Kang    신고 관리 공통코드와 API 경로 추가
 * 2026-08-07        SeungHyeon.Kang    관리자 업로드 이미지 조회 경로 추가
 * 2026-08-12        SeungHyeon.Kang    알림 아이콘 관리 API 경로 추가
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

    /** 팝업 사용 화면 구분 공통코드 */
    public static final String POPU_SITU = "POPU_SITU";

    /** 공지사항 카테고리 공통코드 */
    public static final String NOTI_CATE = "NOTI_CATE";

    /** 서비스 정보 카테고리 공통코드 */
    public static final String SVIF_CATE = "SVIF_CATE";

    /** 사용자 조회 이력의 공지사항 대상 유형 */
    public static final String VIEW_TYPE_NOTICE = "NOTICE";

    /** 스케줄러 구분 공통코드 */
    public static final String SCHD_CODE = "SCHD_CODE";

    /** 회원 상태 공통코드 */
    public static final String USER_STAT = "USER_STAT";

    /** 로그인 제공자 공통코드 */
    public static final String USER_PROV = "USER_PROV";

    /** 회원 탈퇴 유형 공통코드 */
    public static final String WTHD_TYPE = "WTHD_TYPE";

    /** 회원 탈퇴 사유 공통코드 */
    public static final String WTHD_RSON = "WTHD_RSON";

    /** 회원 탈퇴 처리 상태 공통코드 */
    public static final String WTHD_STAT = "WTHD_STAT";

    /** 회원 정지 유형 공통코드 */
    public static final String SPND_TYPE = "SPND_TYPE";

    /** 회원 정지 사유 공통코드 */
    public static final String SPND_RSON = "SPND_RSON";

    /** 회원 정지 처리 상태 공통코드 */
    public static final String SPND_STAT = "SPND_STAT";

    /** 신고 대상 유형 공통코드 */
    public static final String CMPL_TAGT = "CMPL_TAGT";

    /** 신고 사유 공통코드 */
    public static final String CMPL_RSON = "CMPL_RSON";

    /** 신고 처리 상태 공통코드 */
    public static final String CMPL_STAT = "CMPL_STAT";

    /** 사용자 신고 대상 유형 */
    public static final String CMPL_TARGET_USER = "CMPL_USER";

    /** 신고 접수 상태 */
    public static final String CMPL_STATUS_RECEIVED = "CMPL_RECEIVED";

    /** 신고 검토 중 상태 */
    public static final String CMPL_STATUS_REVIEWING = "CMPL_REVIEWING";

    /** 신고 조치 완료 상태 */
    public static final String CMPL_STATUS_ACTIONED = "CMPL_ACTIONED";

    /** 신고 반려 상태 */
    public static final String CMPL_STATUS_REJECTED = "CMPL_REJECTED";

    /** 정지 회원 상태 */
    public static final String USER_STAT_SUSPENDED = "SUSPENDED";

    /** 영구 삭제 대기 회원 상태 */
    public static final String USER_STAT_DELETE_PENDING = "DELETE_PENDING";

    /** 회원 상태 변경 Outbox 이벤트 유형 */
    public static final String EVENT_TYPE_USER_STATUS_CHANGED = "USER_STATUS_CHANGED";

    /** 사용자 서버 회원 상태 반영 대기 */
    public static final String USER_STATUS_SYNC_PENDING = "PENDING";

    /** 사용자 서버 회원 상태 반영 완료 */
    public static final String USER_STATUS_SYNC_COMPLETED = "COMPLETED";

    /** 기간 정지 유형 */
    public static final String SPND_TYPE_PERIOD = "PERIOD";

    /** 무기한 정지 유형 */
    public static final String SPND_TYPE_INDEFINITE = "INDEFINITE";

    /** 적용 중 정지 상태 */
    public static final String SPND_STAT_ACTIVE = "ACTIVE";

    /** 관리자 해제 정지 상태 */
    public static final String SPND_STAT_RELEASED = "RELEASED";

    /** 기간 만료 정지 상태 */
    public static final String SPND_STAT_EXPIRED = "EXPIRED";

    /** 최고 관리자 권한 코드 */
    public static final String AUTH_CODE_SUPER = "SUPER";

    /** 상위 메뉴 SUBX NUMB 값 */
    public static final String TOP_MENU_SUBX_NUMB = "0";

    /** 메뉴 기본 정렬 순서 */
    public static final int DEFAULT_MENU_SORT_ORDR = 1;

    /** 관리자 URL 접두어 */
    public static final String ADMIN_URL_PREFIX = "/sadari/adm";

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

    /** 사이드바 메뉴 API URL */
    public static final String API_MENU_SIDEBAR = "/api/menus/sidebar";

    /** 메뉴 API URL 패턴 */
    public static final String API_MENUS_PATTERN = "/api/menus/**";

    /** 사용자 메뉴 API URL 패턴 */
    public static final String API_USER_MENUS_PATTERN = "/api/user-menus/**";

    /** 코드관리 API URL 패턴 */
    public static final String API_CODE_MANAGE_PATTERN = "/api/code-manage/**";

    /** 알림 템플릿 관리 API URL 패턴 */
    public static final String API_ALIM_TEMP_PATTERN = "/api/alim-temps/**";

    /** 알림 아이콘 관리 API URL 패턴 */
    public static final String API_ALIM_ICON_PATTERN = "/api/alim-icons/**";

    /** 팝업 콘텐츠 관리 API URL 패턴 */
    public static final String API_POPUP_CONTENT_PATTERN = "/api/popup-contents/**";

    /** 권한그룹 관리 API URL 패턴 */
    public static final String API_AUTH_GROUP_PATTERN = "/api/auth-groups/**";

    /** 관리자 권한 부여 API URL 패턴 */
    public static final String API_ADMIN_AUTHS_PATTERN = "/api/admin-auths/**";

    /** 메뉴 권한 조회 API URL */
    public static final String API_MENU_PERMISSION = "/api/menu-permissions";

    /** 직원 API URL 패턴 */
    public static final String API_EMPLOYEES_PATTERN = "/api/employees/**";

    /** 직원 API URL 접두어 */
    public static final String API_EMPLOYEES_PREFIX = "/api/employees";

    /** 메뉴 API URL 접두어 */
    public static final String API_MENUS_PREFIX = "/api/menus";

    /** 사용자 메뉴 API URL 접두어 */
    public static final String API_USER_MENUS_PREFIX = "/api/user-menus";

    /** 코드관리 API URL 접두어 */
    public static final String API_CODE_MANAGE_PREFIX = "/api/code-manage";

    /** 알림 템플릿 관리 API URL 접두어 */
    public static final String API_ALIM_TEMP_PREFIX = "/api/alim-temps";

    /** 알림 아이콘 관리 API URL 접두어 */
    public static final String API_ALIM_ICON_PREFIX = "/api/alim-icons";

    /** 팝업 콘텐츠 관리 API URL 접두어 */
    public static final String API_POPUP_CONTENT_PREFIX = "/api/popup-contents";

    /** 공지사항 관리 API URL 접두어 */
    public static final String API_NOTICE_PREFIX = "/api/notices";

    /** 서비스 정보 관리 API URL 접두어 */
    public static final String API_SERVICE_INFO_PREFIX = "/api/service-info";

    /** 권한그룹 관리 API URL 접두어 */
    public static final String API_AUTH_GROUP_PREFIX = "/api/auth-groups";

    /** 관리자 권한 부여 API URL 접두어 */
    public static final String API_ADMIN_AUTHS_PREFIX = "/api/admin-auths";

    /** 스케줄러 로그 API 경로 */
    public static final String API_SCHEDULE_LOGS_PREFIX = "/api/schedule-logs";

    /** 현재 사용자 관리 API URL 접두어 */
    public static final String API_CURRENT_USERS_PREFIX = "/api/current-users";

    /** 현재 사용자 관리 API URL 패턴 */
    public static final String API_CURRENT_USERS_PATTERN = "/api/current-users/**";

    /** 신고 관리 API URL 접두어 */
    public static final String API_COMPLAINTS_PREFIX = "/api/complaints";

    /** 신고 관리 API URL 패턴 */
    public static final String API_COMPLAINTS_PATTERN = "/api/complaints/**";

    /** 관리자 업로드 이미지 조회 URL 패턴 */
    public static final String API_UPLOADS_PATTERN = "/uploads/**";

    /**
     * 공통 상수 생성 방지
     * @author SeungHyeon.Kang
     * @return
     */
    private Constant() {
    }
}

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
 * 2026-08-13        SeungHyeon.Kang    사용자 통계 API 경로 추가
 * 2026-08-13        SeungHyeon.Kang    사용자 이탈 추세 처리 유형 추가
 * 2026-08-22        SeungHyeon.Kang    신고 자동 조치 조회 상태 추가
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
    // 기타 신고 사유 세부코드
    public static final String CMPL_REASON_OTHER = "CMPL_OTHER";

    /** 신고 처리 상태 공통코드 */
    public static final String CMPL_STAT = "CMPL_STAT";

    /** 신고 자동 조치 유형 공통코드 */
    public static final String CMPL_ACTN = "CMPL_ACTN";

    /** 신고 자동 조치 결과 공통코드 */
    public static final String CMPL_RSLT = "CMPL_RSLT";

    /** 사용자 신고 대상 유형 */
    public static final String CMPL_TARGET_USER = "CMPL_USER";

    // 독후감 신고 대상 유형
    public static final String CMPL_TARGET_BOOK_REPORT = "CMPL_BOOK_REPORT";

    // 댓글 또는 답글 신고 대상 유형
    public static final String CMPL_TARGET_REPLY = "CMPL_REPLY";

    // 독서 모임 신고 대상 유형
    public static final String CMPL_TARGET_CLUB = "CMPL_CLUB";

    // 프로필 사진 신고 대상 유형
    public static final String CMPL_TARGET_PROFILE_IMAGE = "CMPL_PROF_IMAGE";

    // 배경사진 신고 대상 유형
    public static final String CMPL_TARGET_BACKGROUND_IMAGE = "CMPL_BG_IMAGE";

    // 한줄소개 신고 대상 유형
    public static final String CMPL_TARGET_INTRODUCTION = "CMPL_INTRO";

    // 독후감 비공개 전환 자동 조치 유형
    public static final String CMPL_ACTION_HIDE_REPORT = "CMPL_HIDE_REPORT";

    // 댓글 논리 삭제 자동 조치 유형
    public static final String CMPL_ACTION_DELETE_REPLY = "CMPL_DEL_REPLY";

    // 프로필 사진 초기화 자동 조치 유형
    public static final String CMPL_ACTION_RESET_PROFILE = "CMPL_RESET_PROF";

    // 배경사진 초기화 자동 조치 유형
    public static final String CMPL_ACTION_RESET_BACKGROUND = "CMPL_RESET_BG";

    // 한줄소개 초기화 자동 조치 유형
    public static final String CMPL_ACTION_CLEAR_INTRO = "CMPL_CLEAR_INTRO";

    // 독후감 좋아요 대상 유형
    public static final String LIKE_TARGET_REPORT = "REPORT";

    // 댓글 또는 답글 좋아요 대상 유형
    public static final String LIKE_TARGET_REPLY = "REPLY";

    /** 신고 접수 상태 */
    public static final String CMPL_STATUS_RECEIVED = "CMPL_RECEIVED";

    /** 신고 검토 중 상태 */
    public static final String CMPL_STATUS_REVIEWING = "CMPL_REVIEWING";

    /** 신고 조치 완료 상태 */
    public static final String CMPL_STATUS_ACTIONED = "CMPL_ACTIONED";

    /** 신고 반려 상태 */
    public static final String CMPL_STATUS_REJECTED = "CMPL_REJECTED";
    // 신고 조치 결과의 신고자 수신 유형
    public static final String CMPL_RECEIVER_REPORTER = "REPORTER";
    // 신고 조치 결과의 피신고자 수신 유형
    public static final String CMPL_RECEIVER_TARGET = "TARGET";
    // 모든 누적 신고 사유가 같은 유형인 요약 코드
    public static final String CMPL_REASON_SUMMARY_SINGLE = "SINGLE";
    // 누적 신고 사유가 둘 이상인 요약 코드
    public static final String CMPL_REASON_SUMMARY_MULTIPLE = "MULTIPLE";
    // 기타 사유만 누적된 요약 코드
    public static final String CMPL_REASON_SUMMARY_OTHER = "OTHER";
    // 사유를 안전하게 확정할 수 없는 요약 코드
    public static final String CMPL_REASON_SUMMARY_UNKNOWN = "UNKNOWN";
    // 관리자 개별 검토 완료 조치 유형
    public static final String CMPL_ACTION_REVIEW = "CMPL_REVIEW_ACTION";
    // 독후감 완전 삭제 수동 조치 유형
    public static final String CMPL_ACTION_DELETE_REPORT = "CMPL_DEL_REPORT";
    // 모임소개 초기화 수동 조치 유형
    public static final String CMPL_ACTION_CLEAR_CLUB = "CMPL_CLEAR_CLUB";

    // 현재 대상 버전의 자동 조치 진행 상태
    public static final String CMPL_PROGRESS_PENDING = "PENDING";

    // 자동 조치로 대상 원본이 비노출된 진행 상태
    public static final String CMPL_PROGRESS_AUTO_ACTIONED = "AUTO_ACTIONED";

    // 관리자 수동 조치로 대상 원본이 비노출된 진행 상태
    public static final String CMPL_PROGRESS_MANUAL_ACTIONED = "MANUAL_ACTIONED";

    // 신고 당시 버전과 현재 대상 버전이 다른 진행 상태
    public static final String CMPL_PROGRESS_VERSION_CHANGED = "VERSION_CHANGED";

    // 현재 신고 대상 원본이 존재하지 않는 진행 상태
    public static final String CMPL_PROGRESS_TARGET_MISSING = "TARGET_MISSING";

    /** 관리자 원본 수동 조치 처리 내용 식별 접두사 */
    public static final String CMPL_MANUAL_PROCESS_PREFIX = "관리자 원본 수동 조치:";

    /** 정지 회원 상태 */
    public static final String USER_STAT_SUSPENDED = "SUSPENDED";

    /** 정상 회원 상태 */
    public static final String USER_STAT_ACTIVE = "ACTIVE";

    /** 영구 삭제 대기 회원 상태 */
    public static final String USER_STAT_DELETE_PENDING = "DELETE_PENDING";

    /** 계정 비활성화 처리 유형 */
    public static final String WTHD_TYPE_SOFT = "SOFT";

    /** 영구 탈퇴 처리 유형 */
    public static final String WTHD_TYPE_HARD = "HARD";

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

    /** 웰컴페이지 관리 API URL 접두어 */
    public static final String API_WELCOME_PAGE_PREFIX = "/api/welcome-pages";

    /** 웰컴페이지 관리 API URL 패턴 */
    public static final String API_WELCOME_PAGE_PATTERN = "/api/welcome-pages/**";

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

    /** 사용자 통계 API URL 접두어 */
    public static final String API_USER_STATISTICS_PREFIX = "/api/user-statistics";

    /** 사용자 통계 API URL 패턴 */
    public static final String API_USER_STATISTICS_PATTERN = "/api/user-statistics/**";

    /** 신고 관리 API URL 접두어 */
    public static final String API_COMPLAINTS_PREFIX = "/api/complaints";

    /** 신고 관리 API URL 패턴 */
    public static final String API_COMPLAINTS_PATTERN = "/api/complaints/**";

    /** 고객문의 관리 API URL 접두어 */
    public static final String API_INQUIRIES_PREFIX = "/api/inquiries";

    /** 고객문의 관리 API URL 패턴 */
    public static final String API_INQUIRIES_PATTERN = "/api/inquiries/**";

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

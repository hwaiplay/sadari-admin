package org.sadari.admin.sadariadmin.common.result;

import lombok.Getter;
import org.sadari.admin.sadariadmin.common.util.MessageUtil;

/**
 * fileName       : ResultEnum
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 공통 응답 코드 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    현재 사용자 조회 오류 추가
 * 2026-07-30        SeungHyeon.Kang    회원 이용 정지 관리 오류 추가
 * 2026-08-05        SeungHyeon.Kang    신고 조회와 처리 오류 추가
 * 2026-08-05        OpenAI.Codex       세부코드 계층 검증 오류 추가
 */
@Getter
public enum ResultEnum {

    /** 성공 */
    SUCCESS(200, "success.default"),

    /** 저장 성공 */
    SAVE_SUCCESS(200, "success.save"),

    /** 수정 성공 */
    UPDATE_SUCCESS(200, "success.update"),

    /** 삭제 성공 */
    DELETE_SUCCESS(200, "success.delete"),

    /** 조회 결과 없음 */
    COMMON_NO_DATA(2004, "common.no-data"),

    /** 요청값 검증 실패 */
    COMMON_INVALID_REQUEST(2009, "common.invalid-request"),

    /** 필수값 누락 */
    COMMON_REQUIRED_VALUE(2009, "common.required-value"),

    /** 서버 오류 */
    COMMON_SERVER_ERROR(5000, "common.server-error"),

    /** 인증 실패 */
    AUTH_FAIL(1001, "auth.fail"),

    /** 로그인 필요 */
    AUTH_REQUIRED_LOGIN(1001, "auth.required-login"),

    /** 로그인 요청값 오류 */
    AUTH_INVALID_REQUEST(2009, "auth.invalid-request"),

    /** 로그인 인증 실패 */
    AUTH_INVALID_CREDENTIALS(1001, "auth.invalid-credentials"),

    /** 관리자 권한 코드 오류 */
    AUTH_INVALID_CODE(1004, "auth.invalid-code"),

    /** 접근 권한 없음 */
    FORBIDDEN(1004, "forbidden"),

    /** 메뉴 없음 */
    MENU_NOT_FOUND(2004, "menu.not-found"),

    /** 공통코드 중복 */
    CODE_MASTER_DUPLICATE(2009, "code.master.duplicate"),

    /** 세부코드 중복 */
    CODE_DETAIL_DUPLICATE(2009, "code.detail.duplicate"),

    /** 세부코드 없음 */
    CODE_DETAIL_NOT_FOUND(2004, "code.detail.not-found"),

    /** 상위 세부코드 오류 */
    CODE_DETAIL_PARENT_INVALID(2009, "code.detail.parent.invalid"),

    /** 세부코드 순환 참조 */
    CODE_DETAIL_CYCLE(2009, "code.detail.cycle"),

    /** 하위 세부코드 존재 */
    CODE_DETAIL_HAS_CHILDREN(2009, "code.detail.has-children"),

    /** 알림 템플릿 중복 */
    ALIM_TEMP_DUPLICATE(2009, "alim-temp.duplicate"),

    /** 알림 템플릿 없음 */
    ALIM_TEMP_NOT_FOUND(2004, "alim-temp.not-found"),

    /** 팝업 콘텐츠 중복 */
    POPUP_CONTENT_DUPLICATE(2009, "popup-content.duplicate"),

    /** 팝업 콘텐츠 없음 */
    POPUP_CONTENT_NOT_FOUND(2004, "popup-content.not-found"),

    /** 팝업 콘텐츠 형식 오류 */
    POPUP_CONTENT_INVALID(2009, "popup-content.invalid"),

    /** 팝업 콘텐츠 길이 초과 */
    POPUP_CONTENT_TOO_LONG(2009, "popup-content.too-long"),

    /** 공지사항 없음 */
    NOTICE_NOT_FOUND(2004, "notice.not-found"),

    /** 공지사항 입력 형식 오류 */
    NOTICE_INVALID(2009, "notice.invalid"),

    /** 공지사항 이미지 형식 오류 */
    NOTICE_IMAGE_INVALID(2009, "notice.image.invalid"),

    /** 현재 사용자 없음 */
    CURRENT_USER_NOT_FOUND(2004, "current-user.not-found"),

    /** 적용 중인 회원 정지 중복 */
    USER_SUSPENSION_DUPLICATE(2009, "user-suspension.duplicate"),

    /** 회원 정지 이력 없음 */
    USER_SUSPENSION_NOT_FOUND(2004, "user-suspension.not-found"),

    /** 무기한 정지 권한 없음 */
    USER_SUSPENSION_INDEFINITE_FORBIDDEN(1004, "user-suspension.indefinite-forbidden"),

    /** 신고 없음 */
    COMPLAINT_NOT_FOUND(2004, "complaint.not-found"),

    /** 신고 처리 상태 순서 오류 */
    COMPLAINT_INVALID_TRANSITION(2009, "complaint.invalid-transition"),

    /** 신고 동시 수정 충돌 */
    COMPLAINT_CONFLICT(2009, "complaint.conflict"),

    /** 신고 담당자 처리 권한 없음 */
    COMPLAINT_ASSIGNEE_FORBIDDEN(1004, "complaint.assignee-forbidden"),

    /** 사용자 신고 대상 아님 */
    COMPLAINT_TARGET_NOT_USER(2009, "complaint.target-not-user"),

    /** 권한그룹 중복 */
    AUTH_GROUP_DUPLICATE(2009, "auth-group.duplicate"),

    /** 권한그룹 없음 */
    AUTH_GROUP_NOT_FOUND(2004, "auth-group.not-found"),

    /** 관리자 없음 */
    ADMIN_NOT_FOUND(2004, "admin.not-found");

    /** 응답 코드 */
    private final int code;

    /** 메시지 키 */
    private final String messageKey;

    /**
     * 공통 응답 코드 생성
     * @author SeungHyeon.Kang
     * @param code
     * @param messageKey
     * @return
     */
    ResultEnum(int code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    /**
     * 응답 메시지 조회
     * @author SeungHyeon.Kang
     * @return
     */
    public String getMessage() {
        return MessageUtil.getMessage(messageKey);
    }
}

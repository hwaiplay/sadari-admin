package org.sadari.admin.sadariadmin.common.result;

import lombok.Getter;
import org.sadari.admin.sadariadmin.common.util.MessageUtil;

/**
 * 공통 응답 코드
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

    /** 알림 템플릿 중복 */
    ALIM_TEMP_DUPLICATE(2009, "alim-temp.duplicate"),

    /** 알림 템플릿 없음 */
    ALIM_TEMP_NOT_FOUND(2004, "alim-temp.not-found");

    /** 응답 코드 */
    private final int code;

    /** 메시지 키 */
    private final String messageKey;

    /**
     * 공통 응답 코드 생성
     * @Author SeungHyeon.Kang
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
     * @Author SeungHyeon.Kang
     * @return
     */
    public String getMessage() {
        return MessageUtil.getMessage(messageKey);
    }
}

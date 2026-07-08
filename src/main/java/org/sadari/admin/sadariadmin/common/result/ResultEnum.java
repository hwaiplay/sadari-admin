package org.sadari.admin.sadariadmin.common.result;

import lombok.Getter;

/**
 * 공통 응답 코드.
 */
@Getter
public enum ResultEnum {

    /** 조회 결과가 없다. */
    COMMON_NO_DATA(2004, "조회 결과가 없습니다."),

    /** 요청값 검증에 실패했다. */
    COMMON_INVALID_REQUEST(2009, "요청값이 올바르지 않습니다."),

    /** 인증에 실패했다. */
    AUTH_FAIL(1001, "인증에 실패했습니다."),

    /** 접근 권한이 없다. */
    FORBIDDEN(1004, "접근 권한이 없습니다.");

    /** 응답 코드. */
    private final int code;

    /** 응답 메시지. */
    private final String message;

    ResultEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

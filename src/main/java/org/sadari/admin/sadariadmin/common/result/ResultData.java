package org.sadari.admin.sadariadmin.common.result;

import lombok.Getter;

/**
 * 공통 응답 객체.
 */
@Getter
public class ResultData {

    /** 응답 코드. */
    private final int code;

    /** 응답 메시지. */
    private final String message;

    /** 응답 데이터. */
    private final Object data;

    private ResultData(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 성공 응답을 생성한다.
     */
    public static ResultData success() {
        return new ResultData(200, "success", null);
    }

    /**
     * 데이터가 있는 성공 응답을 생성한다.
     */
    public static ResultData success(Object data) {
        return new ResultData(200, "success", data);
    }

    /**
     * 실패 응답을 생성한다.
     */
    public static ResultData fail(ResultEnum resultEnum) {
        return new ResultData(resultEnum.getCode(), resultEnum.getMessage(), null);
    }
}

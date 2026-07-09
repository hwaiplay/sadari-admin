package org.sadari.admin.sadariadmin.common.result;

import lombok.Getter;

/**
 * 공통 응답 객체
 */
@Getter
public class ResultData {

    /** 응답 코드 */
    private final int code;

    /** 응답 메시지 */
    private final String message;

    /** 응답 데이터 */
    private final Object data;

    /**
     * 공통 응답 객체 생성
     * @Author SeungHyeon.Kang
     * @param code
     * @param message
     * @param data
     * @return
     */
    private ResultData(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 성공 응답 생성
     * @Author SeungHyeon.Kang
     * @return
     */
    public static ResultData success() {
        return new ResultData(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), null);
    }

    /**
     * 데이터 포함 성공 응답 생성
     * @Author SeungHyeon.Kang
     * @param data
     * @return
     */
    public static ResultData success(Object data) {
        return new ResultData(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), data);
    }

    /**
     * 성공 메시지 응답 생성
     * @Author SeungHyeon.Kang
     * @param resultEnum
     * @return
     */
    public static ResultData success(ResultEnum resultEnum) {
        return new ResultData(resultEnum.getCode(), resultEnum.getMessage(), null);
    }

    /**
     * 성공 메시지와 데이터 응답 생성
     * @Author SeungHyeon.Kang
     * @param resultEnum
     * @param data
     * @return
     */
    public static ResultData success(ResultEnum resultEnum, Object data) {
        return new ResultData(resultEnum.getCode(), resultEnum.getMessage(), data);
    }

    /**
     * 실패 응답 생성
     * @Author SeungHyeon.Kang
     * @param resultEnum
     * @return
     */
    public static ResultData fail(ResultEnum resultEnum) {
        return new ResultData(resultEnum.getCode(), resultEnum.getMessage(), null);
    }
}


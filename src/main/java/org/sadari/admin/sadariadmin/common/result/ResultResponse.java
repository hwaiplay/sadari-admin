package org.sadari.admin.sadariadmin.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 데이터 없는 공통 응답 객체.
 */
@Getter
@AllArgsConstructor
public class ResultResponse {

    /** 응답 코드. */
    private int code;

    /** 응답 메시지. */
    private String message;
}

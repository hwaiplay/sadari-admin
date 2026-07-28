package org.sadari.admin.sadariadmin.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * fileName       : ResultResponse
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 데이터 없는 공통 응답 객체 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 */
@Getter
@AllArgsConstructor
public class ResultResponse {

    /** 응답 코드 */
    private int code;

    /** 응답 메시지 */
    private String message;
}

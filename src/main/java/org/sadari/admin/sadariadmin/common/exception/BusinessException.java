package org.sadari.admin.sadariadmin.common.exception;

import lombok.Getter;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.springframework.http.HttpStatus;

/**
 * fileName       : BusinessException
 * author         : SeungHyeon.Kang
 * date           : 2026-07-09
 * description    : 업무 예외 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-09        SeungHyeon.Kang    최초 생성
 */
@Getter
public class BusinessException extends RuntimeException {

    /** HTTP 상태 */
    private final HttpStatus status;

    /** 응답 결과 코드 */
    private final ResultEnum resultEnum;

    /**
     * 업무 예외 생성
     * @author SeungHyeon.Kang
     * @param status
     * @param resultEnum
     * @return
     */
    public BusinessException(HttpStatus status, ResultEnum resultEnum) {
        super(resultEnum.getMessage());
        this.status = status;
        this.resultEnum = resultEnum;
    }
}


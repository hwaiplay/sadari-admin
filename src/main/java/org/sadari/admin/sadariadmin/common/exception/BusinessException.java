package org.sadari.admin.sadariadmin.common.exception;

import lombok.Getter;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.springframework.http.HttpStatus;

/**
 * 업무 예외
 */
@Getter
public class BusinessException extends RuntimeException {

    /** HTTP 상태 */
    private final HttpStatus status;

    /** 응답 결과 코드 */
    private final ResultEnum resultEnum;

    /**
     * 업무 예외 생성
     * @Author SeungHyeon.Kang
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


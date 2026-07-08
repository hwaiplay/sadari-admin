package org.sadari.admin.sadariadmin.common.exception;

import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 공통 예외 응답 처리기.
 */
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * 상태 코드 기반 예외를 공통 응답으로 변환한다.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ResultData> handleResponseStatusException(ResponseStatusException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        if (status == HttpStatus.UNAUTHORIZED) {
            return ResponseEntity.status(status).body(ResultData.fail(ResultEnum.AUTH_FAIL));
        }
        if (status == HttpStatus.FORBIDDEN) {
            return ResponseEntity.status(status).body(ResultData.fail(ResultEnum.FORBIDDEN));
        }
        if (status == HttpStatus.NOT_FOUND) {
            return ResponseEntity.status(status).body(ResultData.fail(ResultEnum.COMMON_NO_DATA));
        }
        return ResponseEntity.status(e.getStatusCode()).body(ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST));
    }

    /**
     * 처리되지 않은 예외를 공통 응답으로 변환한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultData> handleException(Exception e) {
        return ResponseEntity.internalServerError().body(ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST));
    }
}

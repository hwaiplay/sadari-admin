package org.sadari.admin.sadariadmin.common.exception;

import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 공통 예외 응답 처리기
 */
@RestControllerAdvice
public class CommonExceptionHandler {

    /** 예외 로그 */
    private static final Logger log = LoggerFactory.getLogger(CommonExceptionHandler.class);

    /**
     * 상태 코드 기반 예외 응답 처리
     * @Author SeungHyeon.Kang
     * @param e
     * @return
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ResultData> handleResponseStatusException(ResponseStatusException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        // 인증 실패 상태 코드는 인증 실패 결과로 변환한다
        if (status == HttpStatus.UNAUTHORIZED) {
            return ResponseEntity.status(status).body(ResultData.fail(ResultEnum.AUTH_FAIL));
        }
        // 접근 거부 상태 코드는 권한 없음 결과로 변환한다
        if (status == HttpStatus.FORBIDDEN) {
            return ResponseEntity.status(status).body(ResultData.fail(ResultEnum.FORBIDDEN));
        }
        // 조회 결과 없음 상태 코드는 데이터 없음 결과로 변환한다
        if (status == HttpStatus.NOT_FOUND) {
            return ResponseEntity.status(status).body(ResultData.fail(ResultEnum.COMMON_NO_DATA));
        }
        return ResponseEntity.status(e.getStatusCode()).body(ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST));
    }

    /**
     * 업무 예외 응답 처리
     * @Author SeungHyeon.Kang
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResultData> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getStatus()).body(ResultData.fail(e.getResultEnum()));
    }

    /**
     * 미처리 예외 응답 처리
     * @Author SeungHyeon.Kang
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultData> handleException(Exception e) {
        log.error("처리되지 않은 예외가 발생했습니다", e);
        return ResponseEntity.internalServerError().body(ResultData.fail(ResultEnum.COMMON_SERVER_ERROR));
    }
}

package com.example.plana.common.exception;

import com.example.plana.dto.common.ResponseBody;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseBody> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[{}] {}", errorCode.getCode(), errorCode.getMessage());
        // ResponseBody.error() 호출
        ResponseBody response = ResponseBody.error(errorCode);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    // 그 외 예상치 못한 모든 에러(500) 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseBody> handleException(Exception e) {
        log.error("[C003] 예상치 못한 오류 발생 | cause: {}", e.getMessage(), e);
        return ResponseEntity
                .status(500)
                .body(ResponseBody.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
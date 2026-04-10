package com.example.plana.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessage()); // 부모인 RuntimeException에 메시지 전달
        this.errorCode = errorCode;
        this.args = args;
    }
}
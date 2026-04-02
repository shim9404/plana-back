package com.example.plana.common.exception;

import lombok.Getter;

public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(400, "C001", "잘못된 요청 형식입니다."),
    METHOD_NOT_ALLOWED(405, "C002", "허용되지 않은 메소드 호출입니다."),
    INTERNAL_SERVER_ERROR(500, "C003", "서버 오류가 발생했습니다."),

    // Auth
    LOGIN_INPUT_INVALID(401, "A001", "아이디 또는 비밀번호가 일치하지 않습니다."),
    TOKEN_EXPIRED(401, "A002", "세션이 만료되었습니다."),
    HANDLE_ACCESS_DENIED(403, "A003", "접근 권한이 없습니다."),
    PASSWORD_MISMATCH(400, "A004", "비밀번호가 일치하지 않습니다."),
    PASSWORD_DUPLICATE(400, "A005", "새 비밀번호가 현재 비밀번호와 동일합니다."),

    // User
    USER_NOT_FOUND(404, "U001", "존재하지 않는 사용자입니다."),
    EMAIL_DUPLICATION(409, "U002", "이미 등록된 이메일입니다.");

    @Getter
    private final int status;
    @Getter
    private final String code;
    @Getter
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

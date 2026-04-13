package com.example.plana.common.response;

import lombok.Getter;

@Getter
public enum SuccessCode {

    // 조회 성공
    SELECT_SUCCESS(200, "S001", "조회에 성공하였습니다."),

    // 생성 성공 (POST)
    INSERT_SUCCESS(201, "S002", "등록이 완료되었습니다."),

    // 수정 성공 (PATCH/PUT)
    UPDATE_SUCCESS(200, "S003", "수정이 정상적으로 처리되었습니다."),

    // 삭제 성공 (DELETE)
    DELETE_SUCCESS(200, "S004", "삭제가 완료되었습니다."),

    // 비밀번호 관련 특화 성공 메시지,
    PASSWORD_UPDATE_SUCCESS(200, "S005", "비밀번호가 안전하게 변경되었습니다."),

    // 로그인 성공
    LOGIN_SUCCESS(200, "S006", "로그인에 성공하였습니다."),
    LOGOUT_SUCCESS(200, "S007", "안전하게 로그아웃되었습니다.");
    
    private final int status;
    private final String code;
    private final String message;

    SuccessCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
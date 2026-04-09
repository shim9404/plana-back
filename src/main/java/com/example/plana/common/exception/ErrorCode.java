package com.example.plana.common.exception;

import lombok.Getter;

public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(400, "C001", "잘못된 요청 형식입니다."),
    RESOURCE_NOT_FOUND(404, "C002", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(405, "C003", "허용되지 않은 메소드 호출입니다."),
    DUPLICATE_RESOURCE(409, "C004", "이미 존재하는 데이터입니다."),
    INTERNAL_SERVER_ERROR(500, "C005", "서버 오류가 발생했습니다."),
    BAD_GATEWAY(502, "C006", "다른 서버에서 잘못된 응답을 받았습니다."),
    TIME_OUT(504, "C007", "응답 시간이 초과되었습니다."),
    MISSING_PARAMETER(400, "C008", "필수 요청 파라미터가 누락되었습니다."),
    INVALID_TYPE_VALUE(400, "C009", "요청 값의 타입이 잘못되었습니다."),
    UNSUPPORTED_MEDIA_TYPE(415, "C010", "지원하지 않는 미디어 타입입니다."),
    DATABASE_ERROR(500, "C011", "데이터베이스 처리 중 오류가 발생했습니다."),
    PAYLOAD_TOO_LARGE(413, "C012", "업로드 가능한 파일 크기를 초과하였습니다."),
    FILE_WRITE_ERROR(500, "C013", "파일 쓰기 중 오류가 발생했습니다."),
    INVALID_FILE_PATH(400, "C014", "유효하지 않은 파일 경로입니다."),

    // Auth
    LOGIN_INPUT_INVALID(401, "A001", "아이디 또는 비밀번호가 일치하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(401, "A002", "세션이 만료되었습니다."),
    HANDLE_ACCESS_DENIED(403, "A003", "접근 권한이 없습니다."),
    PASSWORD_MISMATCH(400, "A004", "비밀번호가 일치하지 않습니다."),
    PASSWORD_DUPLICATE(400, "A005", "새 비밀번호가 현재 비밀번호와 동일합니다."),
    MEMBER_MISMATCH(400, "A006", "회원 정보가 일치하지 않습니다."),
    INVALID_TOKEN(401, "A007", "유효하지 않은 인증 토큰입니다."),
    MALFORMED_TOKEN(401, "A008", "토큰 형식이 잘못되었습니다."),
    REFRESH_TOKEN_EXPIRED(401, "A009", "리프레시 토큰이 만료되었습니다. 다시 로그인하세요."),

    // User
    // User
    USER_NOT_FOUND(404, "U001", "존재하지 않는 사용자입니다."),
    EMAIL_DUPLICATION(409, "U002", "이미 등록된 이메일입니다."),
    ALREADY_WITHDRAWN_MEMBER(400, "U003", "탈퇴처리 중인 사용자입니다."),
    DUPLICATE_NICKNAME(409, "U004", "이미 사용 중인 닉네임입니다."),
    INVALID_PASSWORD_FORMAT(400, "U005", "비밀번호 규칙에 위배됩니다."),
    ACCOUNT_LOCKED(403, "U006", "비밀번호 5회 오류로 계정이 잠겼습니다."),
    SOCIAL_LINK_ERROR(409, "U007", "이미 다른 소셜 계정과 연동되어 있습니다."),

    // Trip
    TRIP_CREATE_FAILED(500, "T001", "여행 생성 중 오류가 발생했습니다."),
    TRIP_DAY_CREATE_FAILED(500, "T002", "여행 일자 생성 중 오류가 발생했습니다."),
    TRIP_SCHEDULE_CREATE_FAILED(500, "T003", "여행 스케줄 생성 중 오류가 발생했습니다."),
    TRIP_NOT_FOUND(404, "T004", "존재하지 않는 여행입니다."),
    TRIP_UPDATE_FAILED(500, "T005", "여행 저장 중 오류가 발생했습니다."),
    TRIP_DAY_DELETE_FAILED(500, "T006", "여행 일자 삭제 중 오류가 발생했습니다."),
    TRIP_SCHEDULE_DELETE_FAILED(500, "T007", "여행 스케줄 삭제 중 오류가 발생했습니다."),
    TRIP_DELETE_FAILED(500, "T008", "여행 삭제 중 오류가 발생했습니다."),
    TRIP_SCHEDULE_SAVE_FAILED(500, "T009", "여행 스케줄 저장 중 오류가 발생했습니다."),
    TRIP_SCHEDULE_NOT_FOUND(404, "T010", "존재하지 않는 여행 스케줄입니다."),
    TRIP_SCHEDULE_REORDER_FAILED(500, "T011", "여행 스케줄 재정렬 중 오류가 발생했습니다."),
    TRIP_DAY_REORDER_FAILED(500, "T012", "여행 일자 재정렬 중 오류가 발생했습니다."),
    TRIP_DAY_NOT_FOUND(500, "T013", "존재하지 않는 여행 일자입니다."),
    TRIP_READ_FAILED(500, "T014", "여행 조회 중 오류가 발생했습니다."),
    TRIP_DAY_READ_FAILED(500, "T015", "여행 일자 조회 중 오류가 발생했습니다."),
    TRIP_SCHEDULE_READ_FAILED(500, "T016", "여행 스케줄 조회 중 오류가 발생했습니다."),
    TRIP_BOOKMARK_CREATE_FAILED(500, "T017", "여행 북마크 등록 중 오류가 발생했습니다."),
    TRIP_BOOKMARK_DELETE_FAILED(500, "T018", "여행 북마크를 삭제할 수 없습니다."),
    TRIP_BOOKMARK_NOT_FOUND(404, "T019", "존재하지 않는 북마크입니다."),

    // Region & Area
    AREA_CREATE_FAILED(500, "R001", "신규 장소 등록 중 오류가 발생했습니다."),

    ;

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

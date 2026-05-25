package com.example.plana.dto.common;

import com.example.plana.common.exception.ErrorCode;
import com.example.plana.common.response.SuccessCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBody<T> {
    @Schema(description = "요청 성공 여부")
    private boolean success;
    @Schema(description = "응답 코드")
    private Integer status;
    @Schema(description = "응답 메세지 코드")
    private String code;
    @Schema(description = "응답 메세지 내용")
    private String message;
    @Schema(description = "응답 데이터", nullable = true)
    private T data;

    // 에러 응답을 위한 정적 팩토리 메서드 추가
    public static <T> ResponseBody<T> error(ErrorCode errorCode, String message) {
        return ResponseBody.<T>builder()
                .success(false)
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .message(message)
                .build();
    }


    // 성공 응답을 위한 메서드 (반환 데이터 O)
    public static <T> ResponseBody<T> success(SuccessCode successCode, T data) {
        return ResponseBody.<T>builder()
                .success(true)
                .status(successCode.getStatus())
                .message(successCode.getMessage())
                .code(successCode.getCode())
                .data(data)
                .build();
    }

    // 성공 응답을 위한 메서드 (반환 데이터 X)
    public static <T> ResponseBody<T> success(SuccessCode successCode) {
        return ResponseBody.<T>builder()
                .success(true)
                .status(successCode.getStatus())
                .message(successCode.getMessage())
                .code(successCode.getCode())
                .data(null)
                .build();
    }
}

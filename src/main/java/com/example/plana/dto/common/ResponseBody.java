package com.example.plana.dto.common;

import com.example.plana.common.exception.ErrorCode;
import com.example.plana.common.response.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBody {
    private boolean success;
    private Integer status;
    private String code;
    private String message;
    private Object data;

    // 에러 응답을 위한 정적 팩토리 메서드 추가
    public static ResponseBody error(ErrorCode errorCode) {
        return ResponseBody.builder()
                .success(false)
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }


    // 성공 응답을 위한 메서드
    public static ResponseBody success(SuccessCode successCode, Object data) {
        return ResponseBody.builder()
                .success(true)
                .status(successCode.getStatus())
                .message(successCode.getMessage())
                .code(successCode.getCode())
                .data(data)
                .build();
    }
}

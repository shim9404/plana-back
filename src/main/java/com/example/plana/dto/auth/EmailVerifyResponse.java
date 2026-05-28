package com.example.plana.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 이메일 인증 상태 응답 DTO
 */
@Getter
@AllArgsConstructor
@Schema(description = "이메일 인증 상태 응답")
public class EmailVerifyResponse {
    @Schema(description = "인증 요청한 이메일", example = "sample@gmail.com")
    private String email;
    @Schema(description = "인증 통과 여부", example = "true")
    private boolean verified;
}

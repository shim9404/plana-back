package com.example.plana.dto.redis;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰 재발급 전 Redis에 저장된 Refresh Token과 요청 토큰 일치 여부 확인용
 */
@Data
@NoArgsConstructor
public class RefreshTokenValidateRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;
}


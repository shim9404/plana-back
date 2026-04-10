package com.example.plana.dto.redis;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RefreshTokenRequest {
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;

    @Positive(message = "ttlMs는 0보다 커야 합니다.")
    private long ttlMs;
}

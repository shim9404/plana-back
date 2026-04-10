package com.example.plana.dto.redis;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 실패 횟수 관리 요청 DTO
 */
@Data
@NoArgsConstructor
public class LoginFailRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;
}


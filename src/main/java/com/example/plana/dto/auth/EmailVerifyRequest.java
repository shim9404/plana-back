package com.example.plana.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 이메일 인증번호 검증 요청 DTO
 */
@Getter
@Setter
public class EmailVerifyRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "인증번호는 필수입니다.")
    private String authCode;
}

package com.example.plana.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 이메일 인증번호 검증 요청 DTO
 */
@Getter
@Setter
@Schema(description = "인증 번호 검증 요청")
public class EmailVerifyRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(description = "인증 번호를 받은 이메일", example = "sample@gmail.com")
    private String email;

    @NotBlank(message = "인증번호는 필수입니다.")
    @Schema(description = "이메일로 받은 코드", example = "231042")
    private String authCode;
}

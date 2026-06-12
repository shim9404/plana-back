package com.example.plana.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 이메일 인증번호 발송 요청 DTO
 */
@Getter
@Setter
@Schema(description = "이메일 인증번호 발송 요청")
public class EmailSendRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(description = "가입할 이메일", example = "sample@gmail.com")
    private String email;
}

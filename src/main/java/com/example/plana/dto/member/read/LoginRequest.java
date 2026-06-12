package com.example.plana.dto.member.read;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
@Schema(description = "회원 로그인 요청")
public class LoginRequest {
    @Email
    @NotBlank
    @Schema(description = "이메일", example = "e@e.com")
    private String email;

    @NotBlank
    @Schema(description = "비밀번호", example = "qwer1234")
    private String password;
}

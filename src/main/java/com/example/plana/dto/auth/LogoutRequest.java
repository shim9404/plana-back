package com.example.plana.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "로그아웃 요청")
public class LogoutRequest {

    @NotBlank
    @Email
    @Schema(description = "로그인한 회원의 email", example = "sample@gmail.com")
    private String email;

    @NotBlank(message = "accessToken은 필수입니다.")
    @Schema(description = "로그인한 회원 정보를 담은 accessToken", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJNMjciLCJyb2xlIjoiTUVNQkVSIiwiaWF0IjoxNzc1ODEwMDg1LCJleHAiOjE3NzU4MTAzODV9.aT8d_pfXS2gCCUCyFGjzK1K-QiutX7sFdr_dPvA3eUt4VN-rxBX4tUPDw4C7aRnOXoG1VonNjWVH3yIBMgJkyQ")
    private String accessToken;
}
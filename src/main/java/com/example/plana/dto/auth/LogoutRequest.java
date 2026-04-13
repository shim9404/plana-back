package com.example.plana.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LogoutRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank(message = "accessToken은 필수입니다.")
    private String accessToken;
}
package com.example.plana.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 이메일 인증 상태 응답 DTO
 */
@Getter
@AllArgsConstructor
public class EmailVerifyResponse {
    private String email;
    private boolean verified;
}

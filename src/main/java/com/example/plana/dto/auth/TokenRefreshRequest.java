package com.example.plana.dto.auth;

import lombok.Getter;

@Getter
public class TokenRefreshRequest {
    private String refreshToken;
}

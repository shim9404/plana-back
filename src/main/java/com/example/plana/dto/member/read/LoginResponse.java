package com.example.plana.dto.member.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String memberId;
    private String role;
    private String accessToken;
    private String refreshToken;
}

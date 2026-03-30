package com.example.plana.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberResponse {
    private String memberId;
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String profileImage;
    private String socialType;
    private String role;
    private String accessToken;
    private String refreshToken;
    private String createDate;
    private String latestDate;
    private String status;
}
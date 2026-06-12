package com.example.plana.dto.member.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 로그인 응답")
public class LoginResponse {
    @Schema(description = "회원 ID", example = "M6")
    private String memberId;
    @Schema(description = "회원 권한", example = "MEMBER")
    private String role;
    @Schema(description = "닉네임", example = "이이")
    private String nickname;
    @Schema(description = "이름", example = "E씨")
    private String name;
    @Schema(description = "액세스 토큰")
    private String accessToken;
    @Schema(description = "리프레시 토큰")
    private String refreshToken;
    @Schema(description = "프로필 이미지", example = "profileImage7")
    private String profileImage;
}

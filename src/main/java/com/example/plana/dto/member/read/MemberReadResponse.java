package com.example.plana.dto.member.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 조회 응답")
public class MemberReadResponse {
    @Schema(description = "회원 ID", example = "M6")
    private String memberId;
    @Schema(description = "이메일", example = "e@e.com")
    private String email;
    @Schema(description = "이름", example = "E씨")
    private String name;
    @Schema(description = "닉네임", example = "이이")
    private String nickname;
    @Schema(description = "프로필 이미지", example = "profileImage7")
    private String profileImage;
}

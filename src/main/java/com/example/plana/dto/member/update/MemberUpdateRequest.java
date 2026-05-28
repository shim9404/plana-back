package com.example.plana.dto.member.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 수정 요청")
public class MemberUpdateRequest {
    @Schema(description = "닉네임", example = "이이")
    private String nickname;
    @Schema(description = "프로필 이미지", example = "profileImage7")
    private String profileImage;
}

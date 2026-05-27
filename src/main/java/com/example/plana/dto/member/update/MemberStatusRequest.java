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
@Schema(description = "회원 정보 탈퇴 요청")
public class MemberStatusRequest {
    @Schema(description = "이메일", example = "e@e.com")
    private String email;
    @Schema(description = "이름", example = "E씨")
    private String name;
    @Schema(description = "현재 비밀번호", example = "qwer1234")
    private String password;
}

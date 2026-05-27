package com.example.plana.dto.member.create;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 가입 시 신규 등록할 회원 정보 요청")
public class MemberCreateRequest {
    @Schema(description = "이메일", example = "e@e.com")
    private String email;
    @Schema(description = "비밀번호", example = "qwer1234")
    private String password;
    @Schema(description = "이름", example = "E씨")
    private String name;
    @Schema(description = "닉네임", example = "이이")
    private String nickname;
}

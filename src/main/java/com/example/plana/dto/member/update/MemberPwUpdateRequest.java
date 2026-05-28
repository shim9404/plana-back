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
@Schema(description = "회원 비밀번호 수정 요청")
public class MemberPwUpdateRequest {
    @Schema(description = "현재 비밀번호", example = "qwer1234")
    private String currentPassword;
    @Schema(description = "새 비밀번호", example = "qwer12345")
    private String newPassword;
}

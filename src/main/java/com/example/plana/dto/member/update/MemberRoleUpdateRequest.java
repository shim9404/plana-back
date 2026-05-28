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
@Schema(description = "사용자 권한 수정 요청")
public class MemberRoleUpdateRequest {
    @Schema(description = "회원 ID", example = "M27")
    private String memberId;
    @Schema(description = "회원 권한", example = "ADMIN")
    private String role;
}

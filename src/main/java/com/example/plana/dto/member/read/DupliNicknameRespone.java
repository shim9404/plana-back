package com.example.plana.dto.member.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "닉네임 중복 응답")
public class DupliNicknameRespone {
    @Schema(description = "새 닉네임", example = "이이")
    private String newNickname;
}

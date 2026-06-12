package com.example.plana.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "이메일 중복 체크 응답")
public class DupliEmailResponse {
    @Schema(description = "사용 가능한 이메일", example="sample@gmail.com")
    private String email;
}

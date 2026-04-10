package com.example.plana.dto.redis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Access Token 블랙리스트 등록 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class BlacklistRequest {
    @NotBlank(message = "accessToken은 필수입니다.")
    private String accessToken;

    @Positive(message = "ttlMs는 0보다 커야 합니다.")
    private long ttlMs;
}

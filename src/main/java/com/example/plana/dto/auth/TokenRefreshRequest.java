package com.example.plana.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "토큰 갱신 요청")
public class TokenRefreshRequest {
    @Schema(description = "refreshToken", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJNMjciLCJyb2xlIjoiTUVNQkVSIiwiaWF0IjoxNzc1ODEwMDg1LCJleHAiOjE3NzU4MTAzODV9.aT8d_pfXS2gCCUCyFGjzK1K-QiutX7sFdr_dPvA3eUt4VN-rxBX4tUPDw4C7aRnOXoG1VonNjWVH3yIBMgJkyQ")
    private String refreshToken;
}

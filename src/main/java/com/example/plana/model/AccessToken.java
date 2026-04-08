package com.example.plana.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// 구글 토큰 엔드포인트에서 내려주는 JSON을 그대로 매핑해서 담기
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)//없는 필드 자동으로 무시함
public class AccessToken {
    // 사용자 프로필 조회 API를 호출
    private String accessToken;//API호출용 토큰
    private String expiresIn;//토큰 유효 시간(초단위)
    private String scope;//토큰으로 접근가능한 범위(openid email profile)
    private String tokenType;//토큰 타입(Bearer)
    private String idToken;//사용자 식별하는 JWT
}

package com.example.plana.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth.kakao")
@Data
public class KakaoConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}

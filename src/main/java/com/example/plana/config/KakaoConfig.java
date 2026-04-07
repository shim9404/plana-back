package com.example.plana.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// yml 파일 내 카카오 API 키 갖고오기
@Component
@ConfigurationProperties(prefix = "kakao.api")
@Data
public class KakaoConfig {
    private String key;
}

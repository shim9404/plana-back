package com.example.plana.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth.visitkorea")
@Data
public class VisitKoreaConfig {
    private String serviceKey;
}

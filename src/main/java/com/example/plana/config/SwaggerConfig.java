package com.example.plana.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        // Security Schema 설정 (JWT)
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("Bearer Token");
        Components components = new Components().addSecuritySchemes("Bearer Token", securityScheme);
        // 문서 정보 설정
        Info info = new Info()
                .title("PLAN A API Docs") // API의 제목
                .description("PLAN A 프로젝트 API 문서") // API에 대한 설명
                .version("0.1.3"); // API의 버전

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}

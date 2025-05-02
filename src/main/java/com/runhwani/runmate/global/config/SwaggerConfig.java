package com.runhwani.runmate.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    private Info apiInfo() {
        return new Info()
            .title("[RunMate] REST API")
            .description("SSAFY 특화 프로젝트 **RunMate 서비스의 API 명세서**입니다.")
            .version("v1.0")
            .contact(new Contact()
                .name("Team RunHwani")
                .email("kimh0414@gmail.com")
                .url("https://k12d107.p.ssafy.io"))
            .license(new License()
                .name("License of API")
                .url("API license URL"));
    }

    private SecurityScheme createApiKeyScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .scheme("bearer");
    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components().addSecuritySchemes("Bearer Authentication", createApiKeyScheme()))
            .info(apiInfo());
    }

    @Bean
    public GroupedOpenApi authApi() {
        String[] paths = {"/api/auth/**"};
        return GroupedOpenApi.builder()
            .group("1. 인증 관리")
            .pathsToMatch(paths)
            .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        String[] paths = {"/api/users/**"};
        return GroupedOpenApi.builder()
            .group("2. 사용자 관리")
            .pathsToMatch(paths)
            .build();
    }

    @Bean
    public GroupedOpenApi runningApi() {
        String[] paths = {"/api/running/**"};
        return GroupedOpenApi.builder()
            .group("3. 러닝 관리")
            .pathsToMatch(paths)
            .build();
    }

    @Bean
    public GroupedOpenApi crewApi() {
        String[] paths = {"/api/crews/**"};
        return GroupedOpenApi.builder()
            .group("4. 크루 관리")
            .pathsToMatch(paths)
            .build();
    }

    @Bean
    public GroupedOpenApi recordApi() {
        String[] paths = {"/api/records/**"};
        return GroupedOpenApi.builder()
            .group("5. 기록 관리")
            .pathsToMatch(paths)
            .build();
    }
} 
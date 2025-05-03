package com.runhwani.runmate.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private Info apiInfo() {
        return new Info()
                .title("RunMate REST API")
                .description("SSAFY 자율 프로젝트 **RunMate 서비스의 API 명세서**입니다.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Team RunHwani")
                        .email("kimh0414@gmail.com")
                        .url("https://industrious-drizzle-496.notion.site/3rd-Project-1d5451c413e080f49fd1ee01180bbe08"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://springdoc.org"));
    }

    private SecurityScheme createApiKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    @Bean
    public OpenAPI openApi() {
        Server server = new Server();
        server.setUrl("");
        server.setDescription("RunMate API Docs");

        return new OpenAPI()
                .addServersItem(server)
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication", createApiKeyScheme()))
                .info(apiInfo());
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1. 인증 API")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi maintainApi() {
        return GroupedOpenApi.builder()
                .group("2. 유지 API")
                .pathsToMatch("/api/maintain/**")
                .build();
    }

    @Bean
    public GroupedOpenApi courseApi() {
        return GroupedOpenApi.builder()
                .group("3. 코스 API")
                .pathsToMatch("/api/courses/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupApi() {
        return GroupedOpenApi.builder()
                .group("4. 그룹 API")
                .pathsToMatch("/api/groups/**")
                .build();
    }

    @Bean
    public GroupedOpenApi careerApi() {
        return GroupedOpenApi.builder()
                .group("5. 커리큘럼 API")
                .pathsToMatch("/api/careers/**")
                .build();
    }

    @Bean
    public GroupedOpenApi diaryApi() {
        return GroupedOpenApi.builder()
                .group("6. 달리기 API")
                .pathsToMatch("/api/diary/**")
                .build();
    }

    @Bean
    public GroupedOpenApi historyApi() {
        return GroupedOpenApi.builder()
                .group("7. 히스토리 API")
                .pathsToMatch("/api/history/**")
                .build();
    }

    @Bean
    public GroupedOpenApi marathonApi() {
        return GroupedOpenApi.builder()
                .group("8. 마라톤 API")
                .pathsToMatch("/api/marathon/**")
                .build();
    }
} 
package com.example.ootd.config.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    // JWT 보안 스키마 정의
    String jwtSchemeName = "jwtAuth";
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
    
    Components components = new Components()
        .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
            .name(jwtSchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT 토큰을 입력하세요. Bearer 접두사는 자동으로 추가됩니다."));

    return new OpenAPI()
        .info(new Info()
            .title("옷장을 부탁해 API 문서")
            .description("옷장을 부탁해 프로젝트의 Swagger API 문서입니다.")
            .version("1.0"))
        .servers(List.of(
            new Server()
                .url("http://localhost:8080")
                .description("로컬 서버")
        ))
        .addSecurityItem(securityRequirement)
        .components(components);
  }

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch("/api/**")
        .build();
  }
}

package com.example.ReservationApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration

@OpenAPIDefinition(info = @Info(title = "Reservation API", version = "1.0", description = "API to manage reservations, users, and warehouses."

))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SwaggerConfig {

    /**
     * OpenAPIの設定を行い、JWT認証用のセキュリティスキームを定義します。
     * Swagger UIでBearerトークン（JWT）を使用できるように設定します。
     * 
     * @return OpenAPIの設定オブジェクト
     */
    // @Bean
    // public OpenAPI customOpenAPI() {
    // return new OpenAPI();
    // .addSecurityItem(new SecurityRequirement().addList("bearerAuth")) //
    // JWT認証のセキュリティ項目を追加
    // .components(new Components() // セキュリティスキームの定義（Bearer Token方式）
    // .addSecuritySchemes("bearerAuth", new SecurityScheme()
    // .name("Authorization") // トークンの名前
    // .type(SecurityScheme.Type.HTTP) // HTTP認証方式
    // .scheme("bearer") // 認証方式：Bearer
    // .bearerFormat("JWT"))); // トークンのフォーマット（JWT）
    // }
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth")); 
    }
}

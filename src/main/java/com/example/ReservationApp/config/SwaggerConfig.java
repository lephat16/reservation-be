package com.example.ReservationApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
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
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth")); 
    }
}

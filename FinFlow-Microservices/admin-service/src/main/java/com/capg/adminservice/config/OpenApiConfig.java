package com.capg.adminservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI adminServiceOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Local / Docker"),
                        new Server().url("http://localhost:8080").description("Via API Gateway")
                ))
                .info(new Info()
                        .title("Admin Service API")
                        .description("Administrative operations — view applications, approve/reject, manage users and reports.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Deepak Kumar")
                                .email("deepak@capg.com")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}

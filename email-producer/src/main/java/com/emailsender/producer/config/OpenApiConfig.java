package com.emailsender.producer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI emailSenderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Email Sender API")
                        .description("Spring Boot Kafka Email Sender - Producer Service API")
                        .version("1.0.0"));
    }
}

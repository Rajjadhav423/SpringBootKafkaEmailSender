package com.example.email.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.email")
@EnableScheduling
@EntityScan(basePackages = "com.example.email")
@EnableJpaRepositories(basePackages = "com.example.email")
public class EmailProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmailProducerApplication.class, args);
    }
}

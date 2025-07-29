package com.amaris.auditspringaiollama;

import com.amaris.auditspringaiollama.service.AuditService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AuditSpringAiOllamaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditSpringAiOllamaApplication.class, args);
    }

}

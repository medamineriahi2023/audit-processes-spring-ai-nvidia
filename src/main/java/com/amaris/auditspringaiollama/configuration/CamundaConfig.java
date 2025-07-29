package com.amaris.auditspringaiollama.configuration;

import com.amaris.auditspringaiollama.service.CamundaAuthService;
import com.amaris.auditspringaiollama.service.OperateApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableScheduling
public class CamundaConfig {

    @Value("${camunda.client.operate.url}")
    private String operateUrl;

    @Value("${camunda.client.operate.client-id}")
    private String clientId;

    @Value("${camunda.client.operate.client-secret}")
    private String clientSecret;

    @Value("${camunda.client.operate.audience}")
    private String audience;

    @Value("${camunda.client.operate.auth-server}")
    private String authServer;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CamundaAuthService authService() {
        return new CamundaAuthService(restTemplate(), authServer, clientId, clientSecret, audience);
    }

    @Bean
    public OperateApiService operateApiService() {
        return new OperateApiService(restTemplate(), operateUrl, authService());
    }


}

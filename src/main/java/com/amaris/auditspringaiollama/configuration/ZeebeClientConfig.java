package com.amaris.auditspringaiollama.configuration;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeClientConfig {

    @Value("${camunda.client.zeebe.gateway-address}")
    private String gatewayAddress;

    @Value("${camunda.client.zeebe.client-id}")
    private String clientId;

    @Value("${camunda.client.zeebe.client-secret}")
    private String clientSecret;

    @Value("${camunda.client.zeebe.audience}")
    private String audience;

    @Value("${camunda.client.zeebe.auth-server}")
    private String authServer;

    @Bean
    public ZeebeClient zeebeClient() {
        OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder()
                .authorizationServerUrl(authServer)
                .audience(audience)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

        return ZeebeClient.newClientBuilder()
                .gatewayAddress(gatewayAddress)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}

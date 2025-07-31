package com.amaris.auditspringaiollama.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ZeebeAuthService {

    private final RestTemplate restTemplate;
    private final String authServer;
    private final String clientId;
    private final String clientSecret;
    private final String audience;

    private String accessToken;
    private LocalDateTime tokenExpiryTime;

    public ZeebeAuthService(RestTemplate restTemplate,
                           @Value("${camunda.client.zeebe.auth-server}") String authServer,
                           @Value("${camunda.client.zeebe.client-id}") String clientId,
                           @Value("${camunda.client.zeebe.client-secret}") String clientSecret,
                           @Value("${camunda.client.zeebe.audience}") String audience) {
        this.restTemplate = restTemplate;
        this.authServer = authServer;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.audience = audience;
    }

    public String getAccessToken() {
        if (accessToken == null || tokenExpiryTime == null ||
                LocalDateTime.now().isAfter(tokenExpiryTime.minusMinutes(5))) {
            refreshToken();
        }
        return accessToken;
    }

    private void refreshToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");
            map.add("audience", audience);
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(authServer, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();
                this.accessToken = (String) tokenResponse.get("access_token");
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");
                this.tokenExpiryTime = LocalDateTime.now().plusSeconds(expiresIn);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'authentification Zeebe", e);
        }
    }
}

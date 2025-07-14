package com.amaris.auditspringaiollama.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.StringReader;

@Service
@RequiredArgsConstructor
public class CamundaModelerService {

    @Value("${camunda.modeler.auth.client_id}")
    private String CLIENT_ID;
    @Value("${camunda.modeler.auth.audience}")
    private String AUDIENCE;
    @Value("${camunda.modeler.auth.grant_type}")
    private String GRANT_TYPE;
    @Value("${camunda.modeler.auth.client_secret}")
    private String CLIENT_SECRET;
    @Value("${camunda.modeler.auth.token_url}")
    private String TOKEN_URL;
    @Value("${camunda.modeler.api.base_url}")
    private String url;

    private final String RESOURCES_PATH = "src/main/resources/bpmn/";

    private final RestTemplate restTemplate;

    private String getAuthToken() {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("client_id", CLIENT_ID);
        requestParams.add("grant_type", GRANT_TYPE);
        requestParams.add("audience", AUDIENCE);
        requestParams.add("client_secret", CLIENT_SECRET);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestParams, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, requestEntity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("access_token")) {
            return (String) responseBody.get("access_token");
        } else {
            throw new RuntimeException("Impossible d'obtenir le token d'authentification Camunda");
        }
    }


    public Map<Object, Object> getFileById(String fileId) {
        String accessToken = getAuthToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String requestUrl = url + "/files/" + fileId;
        ResponseEntity<Map> response = restTemplate.exchange(
            requestUrl,
            org.springframework.http.HttpMethod.GET,
            requestEntity,
            Map.class
        );

        return response.getBody();
    }

    public String getAndSaveBpmnFile(String fileId) throws IOException, ParserConfigurationException, SAXException {
        Map<Object, Object> fileData = getFileById(fileId);

        if (fileData == null || !fileData.containsKey("content")) {
            throw new RuntimeException("Aucun contenu trouvé pour le fichier avec l'ID: " + fileId);
        }

        String xmlContent = (String) fileData.get("content");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlContent)));

        String fileName = "bpmn_" + fileId + ".bpmn";
        if (fileData.containsKey("metadata")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) fileData.get("metadata");
            if (metadata.containsKey("name")) {
                fileName = metadata.get("name") + ".bpmn";
            }
        }

        File directory = new File(RESOURCES_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filePath = RESOURCES_PATH + fileName;

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            outputStream.write(xmlContent.getBytes());
        }

        return filePath;
    }
}

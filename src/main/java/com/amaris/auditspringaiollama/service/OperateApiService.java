package com.amaris.auditspringaiollama.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperateApiService {

    private static final Logger logger = LoggerFactory.getLogger(OperateApiService.class);

    private final RestTemplate restTemplate;
    private final String operateUrl;
    private final CamundaAuthService authService;

    public OperateApiService(RestTemplate restTemplate, String operateUrl,
                             CamundaAuthService authService) {
        this.restTemplate = restTemplate;
        this.operateUrl = operateUrl;
        this.authService = authService;
    }


    /**
     * Récupère le XML de la dernière version BPMN déployée dans Operate
     * Cette méthode trouve la définition de processus la plus récente (par date de déploiement)
     * et retourne son XML
     */
    public String getLatestDeployedBpmnXml() {
        try {
            logger.info("Récupération de la dernière version BPMN déployée par date de déploiement...");

            String url = operateUrl + "/v1/process-definitions/search";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authService.getAccessToken());
            headers.add("Accept", "application/json");

            // D'abord récupérer tous les processus pour analyser les champs disponibles
            Map<String, Object> searchRequest = new HashMap<>();
            searchRequest.put("size", 50); // Récupérer plus de résultats pour trouver le plus récent

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(searchRequest, headers);

            logger.info("Appel à l'API Operate pour récupérer tous les processus: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            logger.info("Réponse de l'API: Status={}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");

                logger.info("Nombre de définitions trouvées: {}", items != null ? items.size() : 0);

                if (items != null && !items.isEmpty()) {
                    // Log pour voir tous les champs disponibles du premier processus
                    Map<String, Object> firstItem = items.get(0);
                    logger.info("Champs disponibles dans le processus: {}", firstItem.keySet());

                    // Puisque l'API ne retourne pas de date de déploiement, utiliser la clé (key) comme indicateur
                    // La clé est généralement un identifiant séquentiel - plus la clé est élevée, plus récent est le déploiement
                    Map<String, Object> latestProcessDefinition = null;
                    long highestKey = -1;

                    for (Map<String, Object> item : items) {
                        logger.info("Processus: ID={}, Version={}, Key={}",
                                   item.get("bpmnProcessId"), item.get("version"), item.get("key"));

                        Object keyObj = item.get("key");
                        if (keyObj != null) {
                            long currentKey;
                            if (keyObj instanceof Number) {
                                currentKey = ((Number) keyObj).longValue();
                            } else {
                                try {
                                    currentKey = Long.parseLong(keyObj.toString());
                                } catch (NumberFormatException e) {
                                    logger.warn("Impossible de parser la clé: {}", keyObj);
                                    continue;
                                }
                            }

                            if (currentKey > highestKey) {
                                highestKey = currentKey;
                                latestProcessDefinition = item;
                            }
                        }
                    }

                    if (latestProcessDefinition != null) {
                        String bpmnProcessId = (String) latestProcessDefinition.get("bpmnProcessId");
                        Object version = latestProcessDefinition.get("version");
                        Object key = latestProcessDefinition.get("key");

                        logger.info("Processus le plus récent sélectionné (par clé la plus élevée) - ID: {}, Version: {}, Key: {}",
                                   bpmnProcessId, version, key);

                        if (key != null) {
                            String xml = getProcessDefinitionXmlById(key.toString());

                            if (xml != null && !xml.trim().isEmpty()) {
                                logger.info("XML BPMN récupéré avec succès (taille: {} caractères)", xml.length());
                                return xml;
                            } else {
                                logger.error("XML vide ou null pour la key: {}", key);
                            }
                        } else {
                            logger.error("Key manquante pour le processus: {}", bpmnProcessId);
                        }
                    } else {
                        logger.warn("Aucun processus valide trouvé");
                    }
                } else {
                    logger.warn("Aucune définition de processus trouvée dans Operate");
                }
            } else {
                logger.error("Erreur lors de la recherche des définitions de processus: Status={}, Body={}",
                           response.getStatusCode(), response.getBody());
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la dernière version BPMN déployée", e);
        }

        return null;
    }

    /**
     * Récupère le XML de la dernière version d'un processus spécifique par son bpmnProcessId
     * Cette méthode trouve la version la plus récente du processus donné
     */
    public String getLatestVersionXmlByBpmnProcessId(String bpmnProcessId) {
        if (bpmnProcessId == null || bpmnProcessId.trim().isEmpty()) {
            logger.error("Le bpmnProcessId est null ou vide");
            return null;
        }

        try {
            logger.info("Récupération de la dernière version du processus: {}", bpmnProcessId);

            String url = operateUrl + "/v1/process-definitions/search";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authService.getAccessToken());

            Map<String, Object> searchRequest = new HashMap<>();
            searchRequest.put("size", 1);
            searchRequest.put("filter", Map.of("bpmnProcessId", bpmnProcessId));
            searchRequest.put("sort", List.of(Map.of("field", "version", "order", "DESC")));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(searchRequest, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");

                if (!items.isEmpty()) {
                    Map<String, Object> latestVersion = items.get(0);
                    Object key = latestVersion.get("key");
                    Object version = latestVersion.get("version");

                    logger.info("Dernière version trouvée pour {}: Version {}, Key: {}",
                               bpmnProcessId, version, key);

                    if (key != null) {
                        return getProcessDefinitionXmlById(key.toString());
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la dernière version du processus: " + bpmnProcessId, e);
        }

        return null;
    }

    /**
     * Récupère le XML BPMN en utilisant l'ID numérique de la définition de processus
     * L'API /v1/process-definitions/{key}/xml attend un Long, pas un String
     */
    public String getProcessDefinitionXmlById(String processDefinitionId) {
        if (processDefinitionId == null || processDefinitionId.trim().isEmpty()) {
            logger.error("Le processDefinitionId est null ou vide");
            return null;
        }

        try {
            String url = operateUrl + "/v1/process-definitions/" + processDefinitionId + "/xml";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authService.getAccessToken());
            headers.setAccept(List.of(
                    MediaType.APPLICATION_XML,
                    MediaType.APPLICATION_JSON,
                    MediaType.TEXT_XML,
                    MediaType.ALL
            ));

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du XML BPMN par ID pour le processus: " + processDefinitionId, e);
        }

        return null;
    }

}
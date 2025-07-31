package com.amaris.auditspringaiollama.service;

import com.amaris.auditspringaiollama.models.ActivityInfo;
import com.amaris.auditspringaiollama.models.EventInfo;
import com.amaris.auditspringaiollama.models.input.SymbolAuditRequest;
import com.amaris.auditspringaiollama.models.output.Response;
import com.amaris.auditspringaiollama.models.output.SymbolAuditResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    private final OpenAiChatModel chatModel;
    private final BpmnFactory bpmnFactory;
    private final OperateApiService operateApiService;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    // ==================== MÉTHODES POUR FICHIER MULTIPART ====================

    public Boolean isValidBpmnFile(MultipartFile file) {
        log.info(ANSI_RED + "Vérification du fichier BPMN..." + ANSI_RESET);
        if (file.isEmpty()) {
            log.error(ANSI_RED + "Le fichier ne peut pas être vide" + ANSI_RESET);
            return false;
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".bpmn")) {
            log.error(ANSI_RED + "Le fichier doit avoir l'extension .bpmn" + ANSI_RESET);
            return false;
        }
        try (InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            return true;
        } catch (Exception e) {
           return false;
        }
    }

    // nchoufou ken les activités fi BPMN file yabdew b verb à l'infinitif ou non
    public List<Response> checkActivitiesIsVerbInfinitiveUsingAI(MultipartFile file) throws IOException {
        logStage1("********************* Stage 1 *********************");
        logStage1("(UTILISANT L'AI) ==>Voir si les activités dans le fichier BPMN sont des verbes à l'infinitif ou non");
        logStage1("***************************************************");
        List<Response> responses = new ArrayList<>();
        List<ActivityInfo> activities;
        try (InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            activities = bpmnFactory.extractActivities(modelInstance);
        } catch (Exception e) {
            throw new BpmnModelException("Erreur lors de la lecture du fichier BPMN: " + e.getMessage(), e);
        }

        for( ActivityInfo activity : activities) {
            String word = activity.getName().trim().split(" ")[0];
            String message = "Réponds uniquement par 1 ou 0 sans explication. Est-ce que le mot \"" + word + "\" est (un verbe et à l'infinitif)? Réponds 1 pour oui, 0 pour non.";
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage1("Aucune réponse reçue pour l'activité: " + activity.getName());
                continue;
            }
            responses.add(new Response( activity.getName(), transformResponse(response)));
            logStage1( activity.getName() + "': " + transformResponse(response));
        }

        return responses;
    }

    public List<Response> checkEventsAreInThePastForm(MultipartFile file) throws IOException {
        logStage2("********************* Stage 2 *********************");
        logStage2("(UTILISANT L'AI) ==> Voir si les événements dans le fichier BPMN sont au passé ou non");
        logStage2("***************************************************");
        List<Response> responses = new ArrayList<>();

        List<EventInfo> events;
        try (InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            events = bpmnFactory.extractEvents(modelInstance);
        } catch (Exception e) {
            throw new IOException("Erreur lors de la lecture du fichier BPMN: " + e.getMessage(), e);
        }

        for( EventInfo eventInfo : events) {
            String[] words = eventInfo.getName().trim().split(" ");
            String lastWord = words[words.length - 1];
            String message = "Réponds uniquement par 1 ou 0 sans explication. Est-ce que le mot \"" + lastWord + "\" est (un verbe dans le passé) ? Réponds 1 pour oui, 0 pour non.";
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage2("Aucune réponse reçue pour l'événement: " + eventInfo.getName());
                continue;
            }
            responses.add(new Response( eventInfo.getName(), transformResponse(response)));
            logStage2( eventInfo.getName() + "': " + transformResponse(response));
        }

        return responses;
    }

    public List<Response> detectAbbreviations(MultipartFile file) throws IOException {
        logStage3("********************** Stage 3 *********************");
        logStage3("(UTILISANT L'AI) ==> Vérifier les abréviations dans le fichier BPMN");
        logStage3("*****************************************************");
        List<Response> responses = new ArrayList<>();

        List<EventInfo> events;
        try (InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            events = bpmnFactory.extractEvents(modelInstance);
        } catch (Exception e) {
            throw new IOException("Erreur lors de la lecture du fichier BPMN: " + e.getMessage(), e);
        }

        for( EventInfo eventInfo : events) {
            String message = "Réponds uniquement par 0 ou 1, sans aucune explication. Analyse le texte suivant : \"" + eventInfo.getName() + "\". Si le texte contient une ou plusieurs abréviations (ex. sigles, acronymes en génerale toute en majuscule), réponds 0. Sinon, réponds 1.";
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage3("Aucune réponse reçue pour l'activité: " + eventInfo.getName());
                continue;
            }
            responses.add(new Response( eventInfo.getName(), transformResponse(response)));

            logStage3( eventInfo.getName() + "': " + transformResponse(response));
        }
        return responses;
    }

    public Map<String, String> checkTheNumberOfStartEvents(MultipartFile file) throws IOException {
        logStage4("********************** Stage 4 *********************");
        logStage4("(UTILISANT LE CODE NATIF) ==> Vérifier le nombre d'événements de début dans le fichier BPMN");
        logStage4("*****************************************************");
        Map<String, String> result = new HashMap<>();
        try(InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(inputStream);
            long startEventCount = bpmnModelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class).size();
            if (startEventCount == 0) {
                logStage4("Aucun événement de début trouvé dans le fichier BPMN. => " + transformResponse("0"));
                result.put("Aucun événement de début trouvé", transformResponse("0"));
            } else if (startEventCount > 1) {
                logStage4("Plus d'un événement de début trouvé dans le fichier BPMN. Nombre d'événements de début: " + startEventCount +" => " + transformResponse("0"));
                result.put("Plus d'un événement de début trouvé", transformResponse("0"));
            } else {
                logStage4("Un seul événement de début trouvé dans le fichier BPMN." + transformResponse("1"));
                result.put("Un seul événement de début trouvé", transformResponse("1"));
            }
        }
        return result;
    }

    public Map<String, String> checkTheNumberOfEndEvents(MultipartFile file) throws IOException {
        logStage5("********************** Stage 5 *********************");
        logStage5("(UTILISANT LE CODE NATIF) ==> Vérifier le nombre d'événements de fin dans le fichier BPMN");
        logStage5("*****************************************************");
        Map<String, String> result = new HashMap<>();
        try(InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(inputStream);
            long endEventsCount = bpmnModelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.EndEvent.class).size();
            if (endEventsCount == 0) {
                logStage5("Aucun événement de fin trouvé dans le fichier BPMN.: " + transformResponse("0"));
                result.put("Aucun événement de fin trouvé", transformResponse("0"));
            } else if (endEventsCount > 0) {
                logStage5("Plus d'un événement de fin trouvé dans le fichier BPMN. Nombre d'événements de fin: " + endEventsCount + " => " + transformResponse("1"));
                result.put("Plus d'un événement de fin trouvé", transformResponse("1"));
            }
        }
        return result;
    }

    /**
     * Récupère le dernier BPMN déployé sur Camunda Operate et le retourne comme MultipartFile
     */
    public String getLatestDeployedBpmnAsMultipartFile() throws Exception {
        log.info("Récupération du dernier BPMN déployé depuis Camunda Operate...");
        return operateApiService.getLatestDeployedBpmnXml();
    }

    // ==================== NOUVELLES MÉTHODES POUR XML DEPUIS OPERATE ====================

    /**
     * Valide un XML BPMN récupéré depuis Operate
     */
    public Boolean isValidBpmnXml(String bpmnXml) {
        log.info(ANSI_RED + "Vérification du XML BPMN..." + ANSI_RESET);
        if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
            log.error(ANSI_RED + "Le XML BPMN ne peut pas être vide" + ANSI_RESET);
            return false;
        }
        try (InputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8))) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            return true;
        } catch (Exception e) {
            log.error(ANSI_RED + "Erreur lors de la validation du XML BPMN: " + e.getMessage() + ANSI_RESET);
            return false;
        }
    }

    /**
     * Analyse les activités du dernier BPMN déployé depuis Operate
     */
    public List<Response> checkActivitiesIsVerbInfinitiveFromOperate(String bpmnXml) {
        try {
            if (bpmnXml == null) {
                log.error("Impossible de récupérer le XML BPMN depuis Operate");
                return new ArrayList<>();
            }
            return checkActivitiesIsVerbInfinitiveUsingAI(bpmnXml);
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse des activités depuis Operate", e);
            return new ArrayList<>();
        }
    }

    /**
     * Vérifie si les activités dans le XML BPMN sont des verbes à l'infinitif
     */
    public List<Response> checkActivitiesIsVerbInfinitiveUsingAI(String bpmnXml) {
        logStage1("********************* Stage 1 *********************");
        logStage1("(UTILISANT L'AI) ==>Voir si les activités dans le XML BPMN sont des verbes à l'infinitif ou non");
        logStage1("***************************************************");
        List<Response> responses = new ArrayList<>();
        List<ActivityInfo> activities;

        try (InputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8))) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            activities = bpmnFactory.extractActivities(modelInstance);
        } catch (Exception e) {
            throw new BpmnModelException("Erreur lors de la lecture du XML BPMN: " + e.getMessage(), e);
        }

        for( ActivityInfo activity : activities) {
            String word = activity.getName().trim().split(" ")[0];
            String message = "Réponds uniquement par 1 ou 0 sans explication. Est-ce que le mot \"" + word + "\" est (un verbe et à l'infinitif)? Réponds 1 pour oui, 0 pour non.";
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage1("Aucune réponse reçue pour l'activité: " + activity.getName());
                continue;
            }
            responses.add(new Response( activity.getName(), transformResponse(response)));
            logStage1( activity.getName() + "': " + transformResponse(response));
        }

        return responses;
    }

    /**
     * Analyse les événements du dernier BPMN déployé depuis Operate
     */
    public List<Response> checkEventsAreInThePastFormFromOperate() {
        try {
            String bpmnXml = operateApiService.getLatestDeployedBpmnXml();
            if (bpmnXml == null) {
                log.error("Impossible de récupérer le XML BPMN depuis Operate");
                return new ArrayList<>();
            }
            return checkEventsAreInThePastForm(bpmnXml);
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse des événements depuis Operate", e);
            return new ArrayList<>();
        }
    }

    /**
     * Vérifie si les événements dans le XML BPMN sont au passé
     */
    public List<Response> checkEventsAreInThePastForm(String bpmnXml) {
        logStage2("********************* Stage 2 *********************");
        logStage2("(UTILISANT L'AI) ==> Voir si les événements dans le XML BPMN sont au passé ou non");
        logStage2("***************************************************");
        List<Response> responses = new ArrayList<>();
        List<EventInfo> events;

        try (InputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8))) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            events = bpmnFactory.extractEvents(modelInstance);
        } catch (Exception e) {
            throw new BpmnModelException("Erreur lors de la lecture du XML BPMN: " + e.getMessage(), e);
        }

        for( EventInfo eventInfo : events) {
            String[] words = eventInfo.getName().trim().split(" ");
            String lastWord = words[words.length - 1];
            String message = "Réponds uniquement par 1 ou 0 sans explication. Est-ce que le mot \"" + lastWord + "\" est (un verbe dans le passé) ? Réponds 1 pour oui, 0 pour non.";
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage2("Aucune réponse reçue pour l'événement: " + eventInfo.getName());
                continue;
            }
            responses.add(new Response(eventInfo.getName(), transformResponse(response)));
            logStage2( eventInfo.getName() + "': " + transformResponse(response));
        }

        return responses;
    }

    /**
     * Détecte les abréviations dans le dernier BPMN déployé depuis Operate
     */
    public List<Response> detectAbbreviationsFromOperate() {
        try {
            String bpmnXml = operateApiService.getLatestDeployedBpmnXml();
            if (bpmnXml == null) {
                log.error("Impossible de récupérer le XML BPMN depuis Operate");
                return new ArrayList<>();
            }
            return detectAbbreviations(bpmnXml);
        } catch (Exception e) {
            log.error("Erreur lors de la détection des abréviations depuis Operate", e);
            return new ArrayList<>();
        }
    }

    /**
     * Détecte les abréviations dans le XML BPMN
     */
    public List<Response> detectAbbreviations(String bpmnXml) {
        logStage3("********************** Stage 3 *********************");
        logStage3("(UTILISANT L'AI) ==> Vérifier les abréviations dans le XML BPMN");
        logStage3("*****************************************************");
        List<Response> responses = new ArrayList<>();
        List<EventInfo> events;

        try (InputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8))) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            events = bpmnFactory.extractEvents(modelInstance);
        } catch (Exception e) {
            throw new BpmnModelException("Erreur lors de la lecture du XML BPMN: " + e.getMessage(), e);
        }

        for( EventInfo eventInfo : events) {
            String message = "Réponds uniquement par 0 ou 1, sans aucune explication. Analyse le texte suivant : \"" + eventInfo.getName() + "\". Si le texte contient une ou plusieurs abréviations (ex. sigles, acronymes, ou mots tronqués utilisés de manière abrégée), réponds 0. Sinon, réponds 1.";
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage3("Aucune réponse reçue pour l'activité: " + eventInfo.getName());
                continue;
            }
            responses.add(new Response( eventInfo.getName(), transformResponse(response)));
            logStage3( eventInfo.getName() + "': " + transformResponse(response));
        }
        return responses;
    }

    /**
     * Vérifie le nombre d'événements de début dans le dernier BPMN déployé depuis Operate
     */
    public Map<String, String> checkTheNumberOfStartEventsFromOperate() {
        try {
            String bpmnXml = operateApiService.getLatestDeployedBpmnXml();
            if (bpmnXml == null) {
                log.error("Impossible de récupérer le XML BPMN depuis Operate");
                return new HashMap<>();
            }
            return checkTheNumberOfStartEvents(bpmnXml);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des événements de début depuis Operate", e);
            return new HashMap<>();
        }
    }

    /**
     * Vérifie le nombre d'événements de début dans le XML BPMN
     */
    public Map<String, String> checkTheNumberOfStartEvents(String bpmnXml) {
        logStage4("********************** Stage 4 *********************");
        logStage4("(UTILISANT LE CODE NATIF) ==> Vérifier le nombre d'événements de début dans le XML BPMN");
        logStage4("*****************************************************");
        Map<String, String> result = new HashMap<>();

        try (InputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8))) {
            BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(inputStream);
            long startEventCount = bpmnModelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class).size();
            if (startEventCount == 0) {
                logStage4("Aucun événement de début trouvé dans le XML BPMN. => " + transformResponse("0"));
                result.put("Aucun événement de début trouvé", transformResponse("0"));
            } else if (startEventCount > 1) {
                logStage4("Plus d'un événement de début trouvé dans le XML BPMN. Nombre d'événements de début: " + startEventCount +" => " + transformResponse("0"));
                result.put("Plus d'un événement de début trouvé", transformResponse("0"));
            } else {
                logStage4("Un seul événement de début trouvé dans le XML BPMN." + transformResponse("1"));
                result.put("Un seul événement de début trouvé", transformResponse("1"));
            }
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des événements de début", e);
            result.put("Erreur lors de la vérification", transformResponse("0"));
        }
        return result;
    }

    /**
     * Vérifie le nombre d'événements de fin dans le dernier BPMN déployé depuis Operate
     */
    public Map<String, String> checkTheNumberOfEndEventsFromOperate() {
        try {
            String bpmnXml = operateApiService.getLatestDeployedBpmnXml();
            if (bpmnXml == null) {
                log.error("Impossible de récupérer le XML BPMN depuis Operate");
                return new HashMap<>();
            }
            return checkTheNumberOfEndEvents(bpmnXml);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des événements de fin depuis Operate", e);
            return new HashMap<>();
        }
    }

    /**
     * Vérifie le nombre d'événements de fin dans le XML BPMN
     */
    public Map<String, String> checkTheNumberOfEndEvents(String bpmnXml) {
        logStage5("********************** Stage 5 *********************");
        logStage5("(UTILISANT LE CODE NATIF) ==> Vérifier le nombre d'événements de fin dans le XML BPMN");
        logStage5("*****************************************************");
        Map<String, String> result = new HashMap<>();

        try (InputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8))) {
            BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(inputStream);
            long endEventsCount = bpmnModelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.EndEvent.class).size();
            if (endEventsCount == 0) {
                logStage5("Aucun événement de fin trouvé dans le XML BPMN.: " + transformResponse("0"));
                result.put("Aucun événement de fin trouvé", transformResponse("0"));
            } else if (endEventsCount > 0) {
                logStage5("Plus d'un événement de fin trouvé dans le XML BPMN. Nombre d'événements de fin: " + endEventsCount + " => " + transformResponse("1"));
                result.put("Plus d'un événement de fin trouvé", transformResponse("1"));
            }
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des événements de fin", e);
            result.put("Erreur lors de la vérification", transformResponse("0"));
        }
        return result;
    }

    /**
     * Analyse complète du dernier BPMN déployé depuis Operate
     */
    public Map<String, Object> performCompleteAuditFromOperate() {
        log.info("Début de l'analyse complète du dernier BPMN déployé depuis Operate");
        Map<String, Object> results = new HashMap<>();

        try {
            String bpmnXml = operateApiService.getLatestDeployedBpmnXml();
            if (bpmnXml == null) {
                log.error("Impossible de récupérer le XML BPMN depuis Operate");
                results.put("error", "Impossible de récupérer le BPMN depuis Operate");
                return results;
            }

            results.put("stage1_activities", checkActivitiesIsVerbInfinitiveUsingAI(bpmnXml));
            results.put("stage2_events", checkEventsAreInThePastForm(bpmnXml));
            results.put("stage3_abbreviations", detectAbbreviations(bpmnXml));
            results.put("stage4_start_events", checkTheNumberOfStartEvents(bpmnXml));
            results.put("stage5_end_events", checkTheNumberOfEndEvents(bpmnXml));

            log.info("Analyse complète terminée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse complète", e);
            results.put("error", "Erreur lors de l'analyse: " + e.getMessage());
        }

        return results;
    }

    /**
     * Audite un symbole individuel en temps réel lors de la saisie
     */
    public SymbolAuditResponse auditSymbolRealTime(SymbolAuditRequest request) {
        log.info(ANSI_CYAN + "Audit en temps réel du symbole: " + request.getNomSymbol() + " (Type: " + request.getTypeSymbol() + ")" + ANSI_RESET);

        SymbolAuditResponse response = new SymbolAuditResponse();
        response.setIdSymbol(request.getIdSymbol());
        response.setErreurs(new ArrayList<>());

        try {
            List<String> erreurs = new ArrayList<>();
            boolean isValid = true;

            switch (request.getTypeSymbol().toLowerCase()) {
                case "task":
                case "activity":
                    String word = request.getNomSymbol().trim().split(" ")[0];
                    String message = "Réponds uniquement par 1 ou 0 sans explication. Est-ce que le mot \"" + word + "\" est (un verbe et à l'infinitif)? Réponds 1 pour oui, 0 pour non.";
                    String aiResponse = this.chatModel.call(message);

                    if (aiResponse == null || !aiResponse.trim().equals("1")) {
                        erreurs.add("L'activité doit commencer par un verbe à l'infinitif");
                        isValid = false;
                    }
                    break;

                case "event":
                    String[] words = request.getNomSymbol().trim().split(" ");
                    String lastWord = words[words.length - 1];
                    String eventMessage = "Réponds uniquement par 1 ou 0 sans explication. Est-ce que le mot \"" + lastWord + "\" est (un verbe dans le passé) ? Réponds 1 pour oui, 0 pour non.";
                    String eventResponse = this.chatModel.call(eventMessage);

                    if (eventResponse == null || !eventResponse.trim().equals("1")) {
                        erreurs.add("L'événement doit se terminer par un verbe au passé");
                        isValid = false;
                    }

                    // Vérifier les abréviations
                    String abbrevMessage = "Réponds uniquement par 0 ou 1, sans aucune explication. Analyse le texte suivant : \"" + request.getNomSymbol() + "\". Si le texte contient une ou plusieurs abréviations (ex. sigles, acronymes en général toute en majuscule), réponds 0. Sinon, réponds 1.";
                    String abbrevResponse = this.chatModel.call(abbrevMessage);

                    if (abbrevResponse == null || !abbrevResponse.contains("1")) {
                        erreurs.add("L'événement ne doit pas contenir d'abréviations");
                        isValid = false;
                    }
                    break;

                case "gateway":
                    // Pour les gateways, vérifier que c'est une question
                    if (!request.getNomSymbol().trim().endsWith("?")) {
                        erreurs.add("La passerelle doit être formulée comme une question");
                        isValid = false;
                    }
                    break;

                default:
                    // Validation générale pour les autres types
                    if (request.getNomSymbol() == null || request.getNomSymbol().trim().isEmpty()) {
                        erreurs.add("Le nom du symbole ne peut pas être vide");
                        isValid = false;
                    }
                    break;
            }

            response.setResultatAudit(isValid);
            response.setErreurs(erreurs);

            log.info(ANSI_GREEN + "Audit terminé pour le symbole " + request.getIdSymbol() + ": " + (isValid ? "Valide" : "Invalide") + ANSI_RESET);

        } catch (Exception e) {
            log.error(ANSI_RED + "Erreur lors de l'audit du symbole: " + e.getMessage() + ANSI_RESET);
            response.setResultatAudit(false);
            response.setErreurs(List.of("Erreur lors de l'audit: " + e.getMessage()));
        }

        return response;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    // Colored logging methods for each stage
    private void logStage1(String message) {
        log.info(ANSI_CYAN + "[STAGE 1] " + message + ANSI_RESET);
    }

    private void logStage2(String message) {
        log.info(ANSI_GREEN + "[STAGE 2] " + message + ANSI_RESET);
    }

    private void logStage3(String message) {
        log.info(ANSI_YELLOW + "[STAGE 3] " + message + ANSI_RESET);
    }

    private void logStage4(String message) {
        log.info(ANSI_BLUE + "[STAGE 4] " + message + ANSI_RESET);
    }

    private void logStage5(String message) {
        log.info(ANSI_PURPLE + "[STAGE 5] " + message + ANSI_RESET);
    }

    public String transformResponse(String response) {
        if (response.trim().contains("1")){
            return "Valide ✅";
        }
        return "Invalide ❌";
    }

    public Map<String,String> generate(String message) {
        return Map.of("generation", this.chatModel.call(message));
    }
}

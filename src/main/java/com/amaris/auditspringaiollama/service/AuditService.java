package com.amaris.auditspringaiollama.service;

import com.amaris.auditspringaiollama.models.ActivityInfo;
import com.amaris.auditspringaiollama.models.EventInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    private final OpenAiChatModel chatModel;
    private final BpmnFactory bpmnFactory;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    // nchoufou ken les activités fi BPMN file yabdew b verb à l'infinitif ou non
    public Map<String,String> checkActivitiesIsVerbInfinitiveUsingAI(MultipartFile file) throws IOException {
        logStage1("********************* Stage 1 *********************");
        logStage1("(UTILISANT L'AI) ==>Voir si les activités dans le fichier BPMN sont des verbes à l'infinitif ou non");
        logStage1("***************************************************");

        List<ActivityInfo> activities;
        try (InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            activities = bpmnFactory.extractActivities(modelInstance);
        } catch (Exception e) {
            throw new IOException("Erreur lors de la lecture du fichier BPMN: " + e.getMessage(), e);
        }

        for( ActivityInfo activity : activities) {
            String word = activity.getName().trim().split(" ")[0];
            String message = "Réponds uniquement par 1 ou 0 sans explication. Est-ce que le mot \"" + word + "\" est (un verbe et à l'infinitif)? Réponds 1 pour oui, 0 pour non.";
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage1("Aucune réponse reçue pour l'activité: " + activity.getName());
                continue;
            }
            logStage1("Réponse de l'IA pour l'activité '" + activity.getName() + "': " + transformResponse(response));
        }

        return null;
    }

    public Map<String,String> checkEventsAreInThePastForm(MultipartFile file) throws IOException {
        logStage2("********************* Stage 2 *********************");
        logStage2("(UTILISANT L'AI) ==> Voir si les événements dans le fichier BPMN sont au passé ou non");
        logStage2("***************************************************");

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
                logStage2("Aucune réponse reçue pour l'activité: " + eventInfo.getName());
                continue;
            }
            logStage2("Réponse de l'IA pour l'activité '" + eventInfo.getName() + "': " + transformResponse(response));
        }

        return null;
    }

    public void detectAbbreviations(MultipartFile file) throws IOException {
        logStage3("********************** Stage 3 *********************");
        logStage3("(UTILISANT L'AI) ==> Vérifier les abréviations dans le fichier BPMN");
        logStage3("*****************************************************");

        List<EventInfo> events;
        try (InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            events = bpmnFactory.extractEvents(modelInstance);
        } catch (Exception e) {
            throw new IOException("Erreur lors de la lecture du fichier BPMN: " + e.getMessage(), e);
        }

        for( EventInfo eventInfo : events) {
            String message = "Réponds uniquement par 0 ou 1, sans aucune explication. Analyse le texte suivant : \"" + eventInfo.getName() + "\". Si le texte contient une ou plusieurs abréviations (ex. sigles, acronymes, ou mots tronqués utilisés de manière abrégée), réponds 0. Sinon, réponds 1.";
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage3("Aucune réponse reçue pour l'activité: " + eventInfo.getName());
                continue;
            }
            logStage3("Réponse de l'IA pour l'activité '" + eventInfo.getName() + "': " + transformResponse(response));
        }
    }

    public void checkTheNumberOfStartEvents(MultipartFile file) throws IOException {
        logStage4("********************** Stage 4 *********************");
        logStage4("(UTILISANT LE CODE NATIF) ==> Vérifier le nombre d'événements de début dans le fichier BPMN");
        logStage4("*****************************************************");

        try(InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(inputStream);
            long startEventCount = bpmnModelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class).size();
            if (startEventCount == 0) {
                logStage4("Aucun événement de début trouvé dans le fichier BPMN. => " + transformResponse("0"));
            } else if (startEventCount > 1) {
                logStage4("Plus d'un événement de début trouvé dans le fichier BPMN. Nombre d'événements de début: " + startEventCount +" => " + transformResponse("0"));
            } else {
                logStage4("Un seul événement de début trouvé dans le fichier BPMN." + transformResponse("1"));
            }
        }
    }

    public void checkTheNumberOfEndEvents(MultipartFile file) throws IOException {
        logStage5("********************** Stage 5 *********************");
        logStage5("(UTILISANT LE CODE NATIF) ==> Vérifier le nombre d'événements de fin dans le fichier BPMN");
        logStage5("*****************************************************");

        try(InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(inputStream);
            long endEventsCount = bpmnModelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.EndEvent.class).size();
            if (endEventsCount == 0) {
                logStage5("Aucun événement de fin trouvé dans le fichier BPMN.: " + transformResponse("0"));
            } else if (endEventsCount > 0) {
                logStage5("Plus d'un événement de fin trouvé dans le fichier BPMN. Nombre d'événements de fin: " + endEventsCount + " => " + transformResponse("1"));
            }
        }
    }

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
package com.amaris.auditspringaiollama.service.impl;

import com.amaris.auditspringaiollama.models.ActivityInfoDto;
import com.amaris.auditspringaiollama.models.EventInfoDto;
import com.amaris.auditspringaiollama.models.output.Response;
import com.amaris.auditspringaiollama.service.IAuditService;
import com.amaris.auditspringaiollama.service.IbpmnFactory;
import com.amaris.auditspringaiollama.configuration.PromptConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService implements IAuditService {
    private final OpenAiChatModel chatModel;
    private final IbpmnFactory bpmnFactory;
    private final PromptConfiguration promptConfiguration;

    // Chemin vers le dossier contenant les fichiers BPMN
    private static final String BPMN_FILES_PATH = "src/main/resources/bpmn/";

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

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
    public List<Response>checkActivitiesIsVerbInfinitiveUsingAI(MultipartFile file) {
        logStage1("********************* Stage 1 *********************");
        logStage1("(UTILISANT L'AI) ==>Voir si les activités dans le fichier BPMN sont des verbes à l'infinitif ou non");
        logStage1("***************************************************");
        List<Response> responses = new ArrayList<>();
        List<ActivityInfoDto> activities;
        try (InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            activities = bpmnFactory.extractActivities(modelInstance);
        } catch (Exception e) {
            throw new BpmnModelException("Erreur lors de la lecture du fichier BPMN: " + e.getMessage(), e);
        }

        for( ActivityInfoDto activity : activities) {
            String word = activity.getName().trim().split(" ")[0];
            String message = promptConfiguration.getInfinitiveVerbPrompt(word);
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage1("Aucune réponse reçue pour l'activité: " + activity.getName());
                continue;
            }
            responses.add(new Response("Réponse de l'IA pour l'activité '" + activity.getName(), transformResponse(response)));
            logStage1("Réponse de l'IA pour l'activité '" + activity.getName() + "': " + transformResponse(response));
        }

        return responses;
    }

    public List<Response> checkEventsAreInThePastForm(MultipartFile file) throws IOException {
        logStage2("********************* Stage 2 *********************");
        logStage2("(UTILISANT L'AI) ==> Voir si les événements dans le fichier BPMN sont au passé ou non");
        logStage2("***************************************************");
        List<Response> responses = new ArrayList<>();

        List<EventInfoDto> events;
        try (InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            events = bpmnFactory.extractEvents(modelInstance);
        } catch (Exception e) {
            throw new IOException("Erreur lors de la lecture du fichier BPMN: " + e.getMessage(), e);
        }

        for( EventInfoDto eventInfoDto : events) {
            String[] words = eventInfoDto.getName().trim().split(" ");
            String lastWord = words[words.length - 1];
            String message = promptConfiguration.getPastTenseWordPrompt(lastWord);
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage2("Aucune réponse reçue pour l'événement: " + eventInfoDto.getName());
                continue;
            }
            responses.add(new Response("Réponse de l'IA pour l'événement '" + eventInfoDto.getName(), transformResponse(response)));
            logStage2("Réponse de l'IA pour l'événement '" + eventInfoDto.getName() + "': " + transformResponse(response));
        }

        return responses;
    }

    public List<Response> detectAbbreviations(MultipartFile file) throws IOException {
        logStage3("********************** Stage 3 *********************");
        logStage3("(UTILISANT L'AI) ==> Vérifier les abréviations dans le fichier BPMN");
        logStage3("*****************************************************");
        List<Response> responses = new ArrayList<>();
        List<EventInfoDto> events;
        try (InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(inputStream);
            events = bpmnFactory.extractEvents(modelInstance);
        } catch (Exception e) {
            throw new IOException("Erreur lors de la lecture du fichier BPMN: " + e.getMessage(), e);
        }

        for( EventInfoDto eventInfoDto : events) {
            String message = promptConfiguration.getAbbreviationDetectionPrompt(eventInfoDto.getName());
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage3("Aucune réponse reçue pour l'activité: " + eventInfoDto.getName());
                continue;
            }
            responses.add(new Response("Réponse de l'IA pour l'activité '" + eventInfoDto.getName(), transformResponse(response)));

            logStage3("Réponse de l'IA pour l'activité '" + eventInfoDto.getName() + "': " + transformResponse(response));
        }
        return responses;
    }

    public List<Response> checkTheNumberOfStartEvents(MultipartFile file) throws IOException {
        logStage4("********************** Stage 4 *********************");
        logStage4("(UTILISANT LE CODE NATIF) ==> Vérifier le nombre d'événements de début dans le fichier BPMN");
        logStage4("*****************************************************");

        List<Response> responses = new ArrayList<>();

        Map<String, String> result = new HashMap<>();
        try(InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(inputStream);
            long startEventCount = bpmnModelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.StartEvent.class).size();
            if (startEventCount == 0) {
                logStage4("Aucun événement de début trouvé dans le fichier BPMN. => " + transformResponse("0"));
                result.put("Aucun événement de début trouvé", transformResponse("0"));
                responses.add(new Response("Aucun événement de début trouvé",transformResponse("0")));
            } else if (startEventCount > 1) {
                logStage4("Plus d'un événement de début trouvé dans le fichier BPMN. Nombre d'événements de début: " + startEventCount +" => " + transformResponse("0"));
                result.put("Plus d'un événement de début trouvé", transformResponse("0"));
                responses.add(new Response("Plus d'un événement de début trouvé",transformResponse("0")));
            } else {
                logStage4("Un seul événement de début trouvé dans le fichier BPMN." + transformResponse("1"));
                result.put("Un seul événement de début trouvé", transformResponse("1"));
                responses.add(new Response("Un seul événement de début trouvé",transformResponse("1")));

            }
        }
        return responses;
    }

    public List<Response> checkTheNumberOfEndEvents(MultipartFile file) throws IOException {
        logStage5("********************** Stage 5 *********************");
        logStage5("(UTILISANT LE CODE NATIF) ==> Vérifier le nombre d'événements de fin dans le fichier BPMN");
        logStage5("*****************************************************");
        Map<String, String> result = new HashMap<>();
        List<Response> responses = new ArrayList<>();
        try(InputStream inputStream = file.getInputStream()) {
            BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(inputStream);
            long endEventsCount = bpmnModelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.EndEvent.class).size();
            if (endEventsCount == 0) {
                logStage5("Aucun événement de fin trouvé dans le fichier BPMN.: " + transformResponse("0"));
                result.put("Aucun événement de fin trouvé", transformResponse("0"));
                responses.add(new Response("Aucun événement de fin trouvé",transformResponse("0")));

            } else if (endEventsCount > 0) {
                logStage5("Plus d'un événement de fin trouvé dans le fichier BPMN. Nombre d'événements de fin: " + endEventsCount + " => " + transformResponse("1"));
                result.put("Plus d'un événement de fin trouvé", transformResponse("1"));
                responses.add(new Response("Plus d'un événement de fin trouvé",transformResponse("1")));
            }
        }
        return responses;
    }

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

    private BpmnModelInstance getBpmnModelFromFile(String fileName) throws IOException {
        String filePath = BPMN_FILES_PATH + fileName;
        if (!fileName.toLowerCase().endsWith(".bpmn")) {
            filePath += ".bpmn";
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("Le fichier " + filePath + " n'existe pas.");
        }

        try (InputStream inputStream = new FileInputStream(path.toFile())) {
            return Bpmn.readModelFromStream(inputStream);
        } catch (Exception e) {
            throw new IOException("Erreur lors de la lecture du fichier BPMN: " + e.getMessage(), e);
        }
    }

    // Implémentation des nouvelles méthodes utilisant les fichiers du dossier resources
    @Override
    public List<Response> checkActivitiesIsVerbInfinitiveUsingAIFromFile(String fileName) throws IOException {
        logStage1("********************* Stage 1 *********************");
        logStage1("(UTILISANT L'AI) ==>Voir si les activités dans le fichier BPMN sont des verbes à l'infinitif ou non");
        logStage1("***************************************************");
        List<Response> responses = new ArrayList<>();
        List<ActivityInfoDto> activities;

        BpmnModelInstance modelInstance = getBpmnModelFromFile(fileName);
        activities = bpmnFactory.extractActivities(modelInstance);

        for (ActivityInfoDto activity : activities) {
            String word = activity.getName().trim().split(" ")[0];
            String message = promptConfiguration.getInfinitiveVerbPrompt(word);
            String response = this.chatModel.call(message);
            if (response == null || response.isEmpty()) {
                logStage1("Aucune réponse reçue pour l'activité: " + activity.getName());
                continue;
            }
            responses.add(new Response("Réponse de l'IA pour l'activité '" + activity.getName(), transformResponse(response)));
            logStage1("Réponse de l'IA pour l'activité '" + activity.getName() + "': " + transformResponse(response));
        }

        return responses;
    }

    @Override
    public List<Response> checkEventsAreInThePastFormFromFile(String fileName) throws IOException {
        logStage2("********************* Stage 2 *********************");
        logStage2("(UTILISANT L'AI) ==> Voir si les événements dans le fichier BPMN sont au passé ou non");
        logStage2("***************************************************");
        List<Response> responses = new ArrayList<>();

        BpmnModelInstance modelInstance = getBpmnModelFromFile(fileName);
        List<EventInfoDto> events = bpmnFactory.extractEvents(modelInstance);

        for (EventInfoDto event : events) {
            if (event.getName() != null && !event.getName().isEmpty()) {
                String name = event.getName().trim();
                String message = promptConfiguration.getPastTenseExpressionPrompt(name);
                String response = this.chatModel.call(message);
                if (response == null || response.isEmpty()) {
                    logStage2("Aucune réponse reçue pour l'événement: " + name);
                    continue;
                }
                responses.add(new Response("Réponse de l'IA pour l'événement '" + name + "'", transformResponse(response)));
                logStage2("Réponse de l'IA pour l'événement '" + name + "': " + transformResponse(response));
            }
        }

        return responses;
    }

    @Override
    public List<Response> detectAbbreviationsFromFile(String fileName) throws IOException {
        logStage3("********************* Stage 3 *********************");
        logStage3("(UTILISANT L'AI) ==> Détecter les abréviations dans le fichier BPMN");
        logStage3("***************************************************");
        List<Response> responses = new ArrayList<>();

        BpmnModelInstance modelInstance = getBpmnModelFromFile(fileName);
        List<ActivityInfoDto> activities = bpmnFactory.extractActivities(modelInstance);
        List<EventInfoDto> events = bpmnFactory.extractEvents(modelInstance);

        // Vérifier les activités
        for (ActivityInfoDto activity : activities) {
            if (activity.getName() != null && !activity.getName().isEmpty()) {
                String name = activity.getName().trim();
                String message = promptConfiguration.getAbbreviationSimplePrompt(name);
                String response = this.chatModel.call(message);
                if (response == null || response.isEmpty()) {
                    logStage3("Aucune réponse reçue pour l'activité: " + name);
                    continue;
                }
                responses.add(new Response("Réponse de l'IA pour l'activité '" + name + "'", transformResponse(response)));
                logStage3("Réponse de l'IA pour l'activité '" + name + "': " + transformResponse(response));
            }
        }

        // Vérifier les événements
        for (EventInfoDto event : events) {
            if (event.getName() != null && !event.getName().isEmpty()) {
                String name = event.getName().trim();
                String message = promptConfiguration.getAbbreviationSimplePrompt(name);
                String response = this.chatModel.call(message);
                if (response == null || response.isEmpty()) {
                    logStage3("Aucune réponse reçue pour l'événement: " + name);
                    continue;
                }
                responses.add(new Response("Réponse de l'IA pour l'événement '" + name + "'", transformResponse(response)));
                logStage3("Réponse de l'IA pour l'événement '" + name + "': " + transformResponse(response));
            }
        }

        return responses;
    }

    @Override
    public List<Response> checkTheNumberOfStartEventsFromFile(String fileName) throws IOException {
        logStage4("********************* Stage 4 *********************");
        logStage4("Vérification du nombre d'événements de départ dans le fichier BPMN");
        logStage4("***************************************************");
        List<Response> responses = new ArrayList<>();

        BpmnModelInstance modelInstance = getBpmnModelFromFile(fileName);
        List<EventInfoDto> startEvents = bpmnFactory.extractEvents(modelInstance);

        int count = startEvents.size();
        if (count > 1) {
            responses.add(new Response("Nombre d'événements de départ", "Le processus contient " + count + " événements de départ, ce qui peut rendre le flux difficile à suivre. Considérez réduire à un seul événement de départ."));
            logStage4(ANSI_RED + "Le processus contient " + count + " événements de départ, ce qui peut rendre le flux difficile à suivre. Considérez réduire à un seul événement de départ." + ANSI_RESET);
        } else if (count == 1) {
            responses.add(new Response("Nombre d'événements de départ", "Le processus contient 1 événement de départ, ce qui est optimal."));
            logStage4(ANSI_GREEN + "Le processus contient 1 événement de départ, ce qui est optimal." + ANSI_RESET);
        } else {
            responses.add(new Response("Nombre d'événements de départ", "Le processus ne contient aucun événement de départ, ce qui est problématique. Ajoutez au moins un événement de départ."));
            logStage4(ANSI_RED + "Le processus ne contient aucun événement de départ, ce qui est problématique. Ajoutez au moins un événement de départ." + ANSI_RESET);
        }

        return responses;
    }

    @Override
    public List<Response> checkTheNumberOfEndEventsFromFile(String fileName) throws IOException {
        logStage5("********************* Stage 5 *********************");
        logStage5("Vérification du nombre d'événements de fin dans le fichier BPMN");
        logStage5("***************************************************");
        List<Response> responses = new ArrayList<>();

        BpmnModelInstance modelInstance = getBpmnModelFromFile(fileName);
        List<EventInfoDto> endEvents = bpmnFactory.extractEvents(modelInstance);

        int count = endEvents.size();
        if (count > 1) {
            responses.add(new Response("Nombre d'événements de fin", "Le processus contient " + count + " événements de fin. Plusieurs événements de fin peuvent être appropriés pour représenter différents résultats possibles."));
            logStage5(ANSI_YELLOW + "Le processus contient " + count + " événements de fin. Plusieurs événements de fin peuvent être appropriés pour représenter différents résultats possibles." + ANSI_RESET);
        } else if (count == 1) {
            responses.add(new Response("Nombre d'événements de fin", "Le processus contient 1 événement de fin, ce qui est adéquat pour un processus simple."));
            logStage5(ANSI_GREEN + "Le processus contient 1 événement de fin, ce qui est adéquat pour un processus simple." + ANSI_RESET);
        } else {
            responses.add(new Response("Nombre d'événements de fin", "Le processus ne contient aucun événement de fin, ce qui est problématique. Ajoutez au moins un événement de fin."));
            logStage5(ANSI_RED + "Le processus ne contient aucun événement de fin, ce qui est problématique. Ajoutez au moins un événement de fin." + ANSI_RESET);
        }

        return responses;
    }
}


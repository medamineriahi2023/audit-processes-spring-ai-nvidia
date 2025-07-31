package com.amaris.auditspringaiollama.service;

import com.amaris.auditspringaiollama.models.DecisionMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MessageTransformService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String transformMessage(String rawMessage) {
        try {
            // Parser le message JSON en liste de Map
            List<Map<String, String>> rawDecisions = objectMapper.readValue(
                rawMessage,
                new TypeReference<List<Map<String, String>>>() {}
            );

            // Transformer chaque décision
            List<DecisionMessage> transformedDecisions = rawDecisions.stream()
                .map(this::transformDecision)
                .toList();

            // Convertir en JSON et retourner
            return objectMapper.writeValueAsString(transformedDecisions);

        } catch (Exception e) {
            log.error("Erreur lors de la transformation du message: {}", e.getMessage());
            // Retourner le message original en cas d'erreur
            return rawMessage;
        }
    }

    private DecisionMessage transformDecision(Map<String, String> rawDecision) {
        String rule = rawDecision.get("rule");
        String decisionText = rawDecision.get("decision");

        boolean decision = transformDecisionToBoolean(decisionText);

        return new DecisionMessage(rule, decision);
    }

    private boolean transformDecisionToBoolean(String decisionText) {
        if (decisionText == null) {
            return false;
        }

        // Vérifier si c'est "Valide ✅"
        if (decisionText.contains("Valide") || decisionText.contains("✅")) {
            return true;
        }

        // Vérifier si c'est "Invalide ❌"
        if (decisionText.contains("Invalide") || decisionText.contains("❌")) {
            return false;
        }

        // Par défaut, retourner false pour les cas non reconnus
        log.warn("Décision non reconnue: {}", decisionText);
        return false;
    }
}

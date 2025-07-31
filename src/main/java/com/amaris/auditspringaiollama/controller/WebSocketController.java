package com.amaris.auditspringaiollama.controller;

import com.amaris.auditspringaiollama.service.MessageTransformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageTransformService messageTransformService;

    @MessageMapping("/test")
    @SendTo("/topic/kafka-messages")
    public String handleTestMessage(String message) {
        log.info("Message de test reçu via WebSocket: {}", message);
        return "Echo: " + message;
    }

    @GetMapping("/api/websocket/test")
    @ResponseBody
    public String testWebSocketEndpoint() {
        try {
            messagingTemplate.convertAndSend("/topic/kafka-messages", "Message de test depuis l'API REST");
            return "Message de test envoyé via WebSocket avec succès";
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du message de test: {}", e.getMessage());
            return "Erreur lors de l'envoi du message de test: " + e.getMessage();
        }
    }

    @GetMapping("/api/websocket/test-transform")
    @ResponseBody
    public String testTransformation() {
        try {
            String testMessage = "[{\"rule\":\"Valider les résultats manuellement\",\"decision\":\"Valide ✅\"},{\"rule\":\"Valider les résultats manuellement\",\"decision\":\"Invalide ❌\"}]";

            String transformedMessage = messageTransformService.transformMessage(testMessage);

            messagingTemplate.convertAndSend("/topic/kafka-messages", transformedMessage);

            return "Message transformé et envoyé: " + transformedMessage;
        } catch (Exception e) {
            log.error("Erreur lors du test de transformation: {}", e.getMessage());
            return "Erreur lors du test: " + e.getMessage();
        }
    }
}

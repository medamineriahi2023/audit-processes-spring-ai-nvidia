package com.amaris.auditspringaiollama.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageTransformService messageTransformService;

    @KafkaListener(topics = "testKafka")
    public void consumeMessage(String message) {
        log.info("Message reçu du topic Kafka 'testKafka': {}", message);

        try {
            String transformedMessage = messageTransformService.transformMessage(message);
            log.info("Message transformé: {}", transformedMessage);

            messagingTemplate.convertAndSend("/topic/kafka-messages", transformedMessage);
            log.info("Message transmis via WebSocket avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la transmission du message via WebSocket: {}", e.getMessage());
        }
    }
}

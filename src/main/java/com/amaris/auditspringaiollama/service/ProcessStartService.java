package com.amaris.auditspringaiollama.service;

import com.amaris.auditspringaiollama.models.ProcessStartResponse;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessStartService {

    private final ZeebeClient zeebeClient;

    public ProcessStartResponse startProcess(String processKey, Map<String, Object> variables) {
        try {
            log.info("Démarrage du processus {} avec variables: {}", processKey, variables);

            ProcessInstanceEvent processInstance;

            if (variables != null && !variables.isEmpty()) {
                processInstance = zeebeClient.newCreateInstanceCommand()
                        .bpmnProcessId(processKey)
                        .latestVersion()
                        .variables(variables)
                        .send()
                        .join();
            } else {
                processInstance = zeebeClient.newCreateInstanceCommand()
                        .bpmnProcessId(processKey)
                        .latestVersion()
                        .send()
                        .join();
            }

            log.info("Processus démarré avec succès. Instance ID: {}", processInstance.getProcessInstanceKey());

            return ProcessStartResponse.builder()
                    .success(true)
                    .processInstanceKey(String.valueOf(processInstance.getProcessInstanceKey()))
                    .processKey(processKey)
                    .message("Processus démarré avec succès")
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors du démarrage du processus {}: {}", processKey, e.getMessage(), e);
            return ProcessStartResponse.builder()
                    .success(false)
                    .processKey(processKey)
                    .message("Erreur lors du démarrage du processus: " + e.getMessage())
                    .build();
        }
    }
}

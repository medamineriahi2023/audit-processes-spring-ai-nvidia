package com.amaris.auditspringaiollama.controller;

import com.amaris.auditspringaiollama.models.ProcessStartRequest;
import com.amaris.auditspringaiollama.models.ProcessStartResponse;
import com.amaris.auditspringaiollama.service.ProcessStartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProcessController {

    private final ProcessStartService processStartService;

    @PostMapping("/start/{processKey}")
    public ResponseEntity<ProcessStartResponse> startProcess(
            @PathVariable String processKey,
            @RequestBody(required = false) ProcessStartRequest request) {

        log.info("Demande de démarrage du processus: {}", processKey);

        Map<String, Object> variables = new HashMap<>();
        if (request != null && request.getVariables() != null) {
            variables = request.getVariables();
        }

        ProcessStartResponse response = processStartService.startProcess(processKey, variables);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/start/{processKey}/no-variables")
    public ResponseEntity<ProcessStartResponse> startProcessWithoutVariables(
            @PathVariable String processKey) {

        log.info("Demande de démarrage du processus sans variables: {}", processKey);

        ProcessStartResponse response = processStartService.startProcess(processKey, new HashMap<>());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/start")
    public ResponseEntity<ProcessStartResponse> startProcessGeneric(
            @RequestBody ProcessStartRequest request) {

        if (request.getProcessKey() == null || request.getProcessKey().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ProcessStartResponse.builder()
                    .success(false)
                    .message("La clé du processus est obligatoire")
                    .build()
            );
        }

        log.info("Demande de démarrage du processus: {}", request.getProcessKey());

        Map<String, Object> variables = request.getVariables() != null ?
            request.getVariables() : new HashMap<>();

        ProcessStartResponse response = processStartService.startProcess(
            request.getProcessKey(), variables);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}

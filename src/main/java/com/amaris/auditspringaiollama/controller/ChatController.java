package com.amaris.auditspringaiollama.controller;

import com.amaris.auditspringaiollama.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final AuditService auditService;



    @PostMapping(value = "/upload/check/verb/infinitive", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> checkVerbInfinitive(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Le fichier ne peut pas être vide");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".bpmn")) {
            return ResponseEntity.badRequest()
                    .body("Le fichier doit avoir l'extension .bpmn");
        }

        var result = auditService.checkActivitiesIsVerbInfinitiveUsingAI(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/health")
    public ResponseEntity<?> health() throws IOException {

        return ResponseEntity.ok("healthy");
    }

    @PostMapping(value = "/check/file/valid", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> checkFileValidity(@RequestParam("file") MultipartFile file) throws IOException {

        return ResponseEntity.ok(auditService.isValidBpmnFile(file));
    }

    @PostMapping(value = "/upload/check/verb/past/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> checkVerbInThePastForm(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Le fichier ne peut pas être vide");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".bpmn")) {
            return ResponseEntity.badRequest()
                    .body("Le fichier doit avoir l'extension .bpmn");
        }

        var result = auditService.checkEventsAreInThePastForm(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/upload/detect/abbreviations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> detectAbbreviations(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Le fichier ne peut pas être vide");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".bpmn")) {
            return ResponseEntity.badRequest()
                    .body("Le fichier doit avoir l'extension .bpmn");
        }

        var result = auditService.detectAbbreviations(file);
        return ResponseEntity.ok(result);
    }


    @PostMapping(value = "/upload/check/number/end/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> checkNumberEndEvents(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Le fichier ne peut pas être vide");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".bpmn")) {
            return ResponseEntity.badRequest()
                    .body("Le fichier doit avoir l'extension .bpmn");
        }

        var result = auditService.checkTheNumberOfEndEvents(file);
        return ResponseEntity.ok(result);
    }


    @PostMapping(value = "/upload/check/number/start/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> checkNumberStartEvents(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Le fichier ne peut pas être vide");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".bpmn")) {
            return ResponseEntity.badRequest()
                    .body("Le fichier doit avoir l'extension .bpmn");
        }

        var result = auditService.checkTheNumberOfStartEvents(file);
        return ResponseEntity.ok(result);
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> auditBpmnFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Le fichier ne peut pas être vide");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".bpmn")) {
            return ResponseEntity.badRequest()
                    .body("Le fichier doit avoir l'extension .bpmn");
        }

        // partie AI
       var result = auditService.checkActivitiesIsVerbInfinitiveUsingAI(file);
       var result2 = auditService.checkEventsAreInThePastForm(file);
       auditService.detectAbbreviations(file);

       // partie code natif
       auditService.checkTheNumberOfStartEvents(file);
       auditService.checkTheNumberOfEndEvents(file);
        return ResponseEntity.ok(result);
    }




}

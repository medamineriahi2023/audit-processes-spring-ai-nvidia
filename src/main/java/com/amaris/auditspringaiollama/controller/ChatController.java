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
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final AuditService auditService;





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


    @GetMapping("/ai/generate")
    public Map<String,String> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return auditService.generate(message);
    }


}

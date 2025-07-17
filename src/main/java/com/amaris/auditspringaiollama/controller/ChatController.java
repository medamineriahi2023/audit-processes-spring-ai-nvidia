package com.amaris.auditspringaiollama.controller;

import com.amaris.auditspringaiollama.service.IAuditService;
import com.amaris.auditspringaiollama.service.NlpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class ChatController {

    private final IAuditService auditService;
    private final NlpService nlpService;

    @GetMapping(value = "/check/verb/infinitive/{fileName}")
    public ResponseEntity<?> checkVerbInfinitive(@PathVariable String fileName) throws IOException {
        var result = auditService.checkActivitiesIsVerbInfinitiveUsingAIFromFile(fileName);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/check/events/past/{fileName}")
    public ResponseEntity<?> checkEventsInPastForm(@PathVariable String fileName) throws IOException {
        var result = auditService.checkEventsAreInThePastFormFromFile(fileName);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/detect/abbreviations/{fileName}")
    public ResponseEntity<?> detectAbbreviations(@PathVariable String fileName) throws IOException {
        var result = auditService.detectAbbreviationsFromFile(fileName);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/check/start-events/{fileName}")
    public ResponseEntity<?> checkStartEvents(@PathVariable String fileName) throws IOException {
        var result = auditService.checkTheNumberOfStartEventsFromFile(fileName);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/check/end-events/{fileName}")
    public ResponseEntity<?> checkEndEvents(@PathVariable String fileName) throws IOException {
        var result = auditService.checkTheNumberOfEndEventsFromFile(fileName);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping(value = "/test/nlp")
    public void test() {
         nlpService.test();
    }
}

package com.amaris.auditspringaiollama.controller;

import com.amaris.auditspringaiollama.service.IAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class ChatController {

    private final IAuditService auditService;

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
}

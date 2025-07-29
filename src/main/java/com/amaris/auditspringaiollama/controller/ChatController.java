package com.amaris.auditspringaiollama.controller;

import com.amaris.auditspringaiollama.models.input.SymbolAuditRequest;
import com.amaris.auditspringaiollama.models.output.SymbolAuditResponse;
import com.amaris.auditspringaiollama.service.AuditService;
import com.amaris.auditspringaiollama.service.OperateApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatController {

    private final AuditService auditService;
    private final OperateApiService operateApiService;


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

    /**
     * Vérifie si les activités d'un XML BPMN fourni sont des verbes à l'infinitif
     */
    @PostMapping(value = "/operate/check/activities/verb/infinitive")
    public ResponseEntity<?> checkActivitiesVerbInfinitiveFromXml() {
        String bpmnXml = operateApiService.getLatestDeployedBpmnXml();
        try {
            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Le XML BPMN ne peut pas être vide");
            }

            var result = auditService.checkActivitiesIsVerbInfinitiveUsingAI(bpmnXml);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de l'analyse des activités: " + e.getMessage());
        }
    }

    // ==================== ENDPOINTS POUR ANALYSE XML DEPUIS OPERATE ====================

    /**
     * Analyse complète d'un XML BPMN fourni
     */
    @PostMapping(value = "/operate/audit/complete")
    public ResponseEntity<?> performCompleteAuditFromXml() {
        String bpmnXml = operateApiService.getLatestDeployedBpmnXml();
        try {
            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Le XML BPMN ne peut pas être vide");
            }

            Map<String, Object> results = new HashMap<>();
            results.put("stage1_activities", auditService.checkActivitiesIsVerbInfinitiveUsingAI(bpmnXml));
            results.put("stage2_events", auditService.checkEventsAreInThePastForm(bpmnXml));
            results.put("stage3_abbreviations", auditService.detectAbbreviations(bpmnXml));
            results.put("stage4_start_events", auditService.checkTheNumberOfStartEvents(bpmnXml));
            results.put("stage5_end_events", auditService.checkTheNumberOfEndEvents(bpmnXml));

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de l'analyse complète: " + e.getMessage());
        }
    }



    @PostMapping(value = "/operate/check/start-events")
    public ResponseEntity<?> checkStartEventsFromXml() {
        String bpmnXml = operateApiService.getLatestDeployedBpmnXml();
        try {
            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Le XML BPMN ne peut pas être vide");
            }

            var result = auditService.checkTheNumberOfStartEvents(bpmnXml);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la vérification des événements de début: " + e.getMessage());
        }
    }

    /**
     * Récupère le XML du dernier BPMN déployé depuis Operate
     */
    @GetMapping(value = "/operate/bpmn/latest/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> getLatestBpmnXml() {
        try {
            String xml = auditService.getLatestDeployedBpmnAsMultipartFile();
            if (xml != null) {
                return ResponseEntity.ok(xml);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la récupération du XML: " + e.getMessage());
        }
    }

    /**
     * Récupère le XML du dernier BPMN déployé depuis Operate (endpoint alternatif)
     */
    @GetMapping(value = "/operate/latest/bpmn", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> getLatestDeployedBpmnXml() {
        try {
            // Utilisation directe de l'OperateApiService pour récupérer le XML
            String xml = operateApiService.getLatestDeployedBpmnXml();
            if (xml != null && !xml.trim().isEmpty()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_XML)
                        .body(xml);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la récupération du dernier BPMN: " + e.getMessage());
        }
    }

    /**
     * Récupère les informations sur le dernier BPMN déployé (métadonnées)
     */
    @GetMapping(value = "/operate/latest/bpmn/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLatestBpmnInfo() {
        try {
            // Utilisation de l'OperateApiService pour récupérer les métadonnées
            String xml = operateApiService.getLatestDeployedBpmnXml();
            if (xml != null && !xml.trim().isEmpty()) {
                // Validation du XML
                boolean isValid = auditService.isValidBpmnXml(xml);

                Map<String, Object> info = new HashMap<>();
                info.put("hasContent", true);
                info.put("xmlSize", xml.length());
                info.put("isValid", isValid);
                info.put("timestamp", System.currentTimeMillis());

                return ResponseEntity.ok(info);
            } else {
                Map<String, Object> info = new HashMap<>();
                info.put("hasContent", false);
                info.put("message", "Aucun BPMN déployé trouvé dans Operate");

                return ResponseEntity.ok(info);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la récupération des informations: " + e.getMessage());
        }
    }

    // ==================== ENDPOINTS POUR ANALYSE XML PERSONNALISÉ ====================


    /**
     * Analyse les événements d'un XML BPMN fourni
     */
    @PostMapping(value = "/xml/check/events/past-form")
    public ResponseEntity<?> checkEventsAreInThePastFormFromXml() {
        String bpmnXml = operateApiService.getLatestDeployedBpmnXml();
        try {
            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Le XML BPMN ne peut pas être vide");
            }

            var result = auditService.checkEventsAreInThePastForm(bpmnXml);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de l'analyse des événements: " + e.getMessage());
        }
    }

    /**
     * Détecte les abréviations dans un XML BPMN fourni
     */
    @PostMapping(value = "/xml/check/abbreviations")
    public ResponseEntity<?> detectAbbreviationsFromXml() {
        String bpmnXml = operateApiService.getLatestDeployedBpmnXml();
        try {
            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Le XML BPMN ne peut pas être vide");
            }

            var result = auditService.detectAbbreviations(bpmnXml);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la détection des abréviations: " + e.getMessage());
        }
    }



    /**
     * Vérifie le nombre d'événements de fin dans un XML BPMN fourni
     */
    @PostMapping(value = "/xml/check/end-events")
    public ResponseEntity<?> checkEndEventsFromXml() {
        String bpmnXml = operateApiService.getLatestDeployedBpmnXml();

        try {
            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Le XML BPMN ne peut pas être vide");
            }

            var result = auditService.checkTheNumberOfEndEvents(bpmnXml);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la vérification des événements de fin: " + e.getMessage());
        }
    }

    /**
     * Valide un XML BPMN fourni
     */
    @PostMapping(value = "/xml/validate")
    public ResponseEntity<?> validateBpmnXml() {
        String bpmnXml = operateApiService.getLatestDeployedBpmnXml();
        try {
            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Le XML BPMN ne peut pas être vide");
            }

            boolean isValid = auditService.isValidBpmnXml(bpmnXml);
            return ResponseEntity.ok(Map.of("valid", isValid));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la validation: " + e.getMessage());
        }
    }

    /**
     * Audit un symbole en temps réel
     */
    @PostMapping(value = "/audit/symbol", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> auditSymbol(@RequestBody SymbolAuditRequest request) {
        try {
            // Validation de la requête
            if (request.getNomSymbol() == null || request.getNomSymbol().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Le nom du symbole ne peut pas être vide");
            }

            if (request.getTypeSymbol() == null || request.getTypeSymbol().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Le type du symbole ne peut pas être vide");
            }

            if (request.getIdSymbol() == null || request.getIdSymbol().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("L'ID du symbole ne peut pas être vide");
            }

            // Appel au service pour l'audit du symbole
            SymbolAuditResponse response = auditService.auditSymbolRealTime(request);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de l'audit du symbole: " + e.getMessage());
        }
    }


}

package com.amaris.auditspringaiollama.service;

import com.amaris.auditspringaiollama.models.output.Response;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IAuditService {

     Boolean isValidBpmnFile(MultipartFile file);
     List<Response> checkActivitiesIsVerbInfinitiveUsingAI(MultipartFile file);
     List<Response> checkEventsAreInThePastForm(MultipartFile file) throws IOException;
     List<Response> detectAbbreviations(MultipartFile file) throws IOException;
     List<Response> checkTheNumberOfStartEvents(MultipartFile file) throws IOException;
     List<Response> checkTheNumberOfEndEvents(MultipartFile file) throws IOException;

     // Nouvelles méthodes utilisant des fichiers du dossier resources
     List<Response> checkActivitiesIsVerbInfinitiveUsingAIFromFile(String fileName) throws IOException;
     List<Response> checkEventsAreInThePastFormFromFile(String fileName) throws IOException;
     List<Response> detectAbbreviationsFromFile(String fileName) throws IOException;
     List<Response> checkTheNumberOfStartEventsFromFile(String fileName) throws IOException;
     List<Response> checkTheNumberOfEndEventsFromFile(String fileName) throws IOException;
}

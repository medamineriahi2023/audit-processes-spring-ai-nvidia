package com.amaris.auditspringaiollama.controller;

import com.amaris.auditspringaiollama.service.IAuditService;
import com.amaris.auditspringaiollama.service.impl.CamundaModelerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("api/v1/modeler")
@RequiredArgsConstructor
public class CamundaModelerController {

    private final CamundaModelerService camundaModelerService;


    @GetMapping("/file/{fileId}")
    public Map<Object, Object> getFile(@PathVariable String fileId) {
        return camundaModelerService.getFileById(fileId);
    }

    @GetMapping("/getAndSaveLatestVersion/{fileId}")
    public String getAndSaveLatestVersion(@PathVariable String fileId) throws IOException, ParserConfigurationException, SAXException {
        return camundaModelerService.getAndSaveBpmnFile(fileId);
    }


}

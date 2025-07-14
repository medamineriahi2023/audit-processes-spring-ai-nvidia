package com.amaris.auditspringaiollama.service.impl;

import com.amaris.auditspringaiollama.models.ActivityInfoDto;
import com.amaris.auditspringaiollama.models.EventInfoDto;
import com.amaris.auditspringaiollama.models.GatewayInfoDto;
import com.amaris.auditspringaiollama.service.IbpmnFactory;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BpmnFactory implements IbpmnFactory {

    public List<ActivityInfoDto> extractActivities(BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementsByType(Task.class)
                .stream()
                .map(task -> new ActivityInfoDto(
                        task.getId(),
                        task.getName() != null ? task.getName() : "Tâche sans nom"
                ))
                .collect(Collectors.toList());
    }

    public List<EventInfoDto> extractEvents(BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementsByType(Event.class)
                .stream()
                .map(event -> new EventInfoDto(
                        event.getId(),
                        event.getName() != null ? event.getName() : "Événement sans nom"
                        ,event.getElementType().getTypeName()))
                .collect(Collectors.toList());
    }

    public List<GatewayInfoDto> extractGateways(BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementsByType(Gateway.class)
                .stream()
                .map(gateway -> new GatewayInfoDto(
                        gateway.getId(),
                        gateway.getName() != null ? gateway.getName() : "Passerelle sans nom"
                ))
                .collect(Collectors.toList());
    }

}

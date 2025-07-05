package com.amaris.auditspringaiollama.service;

import com.amaris.auditspringaiollama.models.ActivityInfo;
import com.amaris.auditspringaiollama.models.EventInfo;
import com.amaris.auditspringaiollama.models.GatewayInfo;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BpmnFactory {

    public List<ActivityInfo> extractActivities(BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementsByType(Task.class)
                .stream()
                .map(task -> new ActivityInfo(
                        task.getId(),
                        task.getName() != null ? task.getName() : "Tâche sans nom"
                ))
                .collect(Collectors.toList());
    }

    public List<EventInfo> extractEvents(BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementsByType(Event.class)
                .stream()
                .map(event -> new EventInfo(
                        event.getId(),
                        event.getName() != null ? event.getName() : "Événement sans nom"
                        ,event.getElementType().getTypeName()))
                .collect(Collectors.toList());
    }

    public List<GatewayInfo> extractGateways(BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementsByType(Gateway.class)
                .stream()
                .map(gateway -> new GatewayInfo(
                        gateway.getId(),
                        gateway.getName() != null ? gateway.getName() : "Passerelle sans nom"
                ))
                .collect(Collectors.toList());
    }

}

package com.amaris.auditspringaiollama.service;

import com.amaris.auditspringaiollama.models.ActivityInfoDto;
import com.amaris.auditspringaiollama.models.EventInfoDto;
import com.amaris.auditspringaiollama.models.GatewayInfoDto;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.List;

public interface IbpmnFactory {
    List<ActivityInfoDto> extractActivities(BpmnModelInstance modelInstance);
    List<EventInfoDto> extractEvents(BpmnModelInstance modelInstance);
    List<GatewayInfoDto> extractGateways(BpmnModelInstance modelInstance);
}

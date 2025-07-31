package com.amaris.auditspringaiollama.models;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessStartRequest {
    private String processKey;
    private Map<String, Object> variables;
}

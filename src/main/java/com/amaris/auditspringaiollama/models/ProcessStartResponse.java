package com.amaris.auditspringaiollama.models;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessStartResponse {
    private boolean success;
    private String processInstanceKey;
    private String processKey;
    private String message;
}

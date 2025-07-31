package com.amaris.auditspringaiollama.models;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecisionMessage {
    private String rule;
    private boolean decision;
}

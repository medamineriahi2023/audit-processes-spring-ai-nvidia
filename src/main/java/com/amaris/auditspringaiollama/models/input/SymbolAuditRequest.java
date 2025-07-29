package com.amaris.auditspringaiollama.models.input;

import lombok.Data;

@Data
public class SymbolAuditRequest {
    private String nomSymbol;
    private String typeSymbol;
    private String idSymbol;
}

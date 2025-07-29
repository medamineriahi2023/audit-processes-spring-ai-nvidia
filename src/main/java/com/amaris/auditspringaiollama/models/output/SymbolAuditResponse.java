package com.amaris.auditspringaiollama.models.output;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SymbolAuditResponse {
    private String idSymbol;
    private Boolean resultatAudit;
    private List<String> erreurs;
}

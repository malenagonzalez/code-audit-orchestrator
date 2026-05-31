package com.auditoria.orchestrator.dto.response;

public record AuditSummaryDto(
        String id,
        String language,
        String status,
        String codeSnippet,
        int issueCount,
        long criticalCount,
        String createdAt
) {}

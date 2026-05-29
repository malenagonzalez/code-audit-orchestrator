package com.codeaudit.orchestrator.dto.response;

public record AuditSummaryDto(
        Long id,
        String language,
        String status,
        String codeSnippet,
        int issueCount,
        long criticalCount,
        String createdAt
) {}

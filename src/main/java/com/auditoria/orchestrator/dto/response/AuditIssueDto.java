package com.auditoria.orchestrator.dto.response;

public record AuditIssueDto(
        String id,
        String severity,
        String category,
        String title,
        String description,
        Integer lineStart,
        Integer lineEnd,
        String refactoredCode
) {}

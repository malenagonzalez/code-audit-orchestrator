package com.codeaudit.orchestrator.dto.response;

public record AuditIssueDto(
        Long id,
        String severity,
        String category,
        String title,
        String description,
        Integer lineStart,
        Integer lineEnd,
        String refactoredCode
) {}

package com.codeaudit.orchestrator.dto.response;

import java.util.List;

public record AuditResultDto(
        Long id,
        String language,
        String status,
        String pedagogicalExplanation,
        String createdAt,
        String completedAt,
        List<AuditIssueDto> issues
) {}

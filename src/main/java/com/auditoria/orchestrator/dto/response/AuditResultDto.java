package com.auditoria.orchestrator.dto.response;

import java.util.List;

public record AuditResultDto(
        String id,
        String language,
        String status,
        String pedagogicalExplanation,
        String createdAt,
        String completedAt,
        List<AuditIssueDto> issues
) {}

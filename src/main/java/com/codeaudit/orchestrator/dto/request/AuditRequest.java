package com.codeaudit.orchestrator.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuditRequest(
        @NotBlank String code,
        @NotBlank String language
) {}

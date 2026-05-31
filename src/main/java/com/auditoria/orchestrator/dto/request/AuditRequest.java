package com.auditoria.orchestrator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AuditRequest(
        @NotBlank String code,
        @NotBlank @Pattern(regexp = "python|java|kotlin") String language
) {}

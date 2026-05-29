package com.codeaudit.orchestrator.dto.response;

public record UserDto(
        Long id,
        String username,
        String email
) {}

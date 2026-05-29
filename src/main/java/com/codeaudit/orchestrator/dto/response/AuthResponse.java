package com.codeaudit.orchestrator.dto.response;

public record AuthResponse(
        String token,
        long expiresIn,
        UserDto user
) {}

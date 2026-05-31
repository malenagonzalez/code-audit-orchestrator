package com.auditoria.orchestrator.dto.response;

public record AuthResponse(String token, long expiresIn, UserDto user) {}

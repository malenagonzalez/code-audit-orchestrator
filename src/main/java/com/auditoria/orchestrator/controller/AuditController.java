package com.auditoria.orchestrator.controller;

import com.auditoria.orchestrator.dto.request.AuditRequest;
import com.auditoria.orchestrator.dto.response.*;
import com.auditoria.orchestrator.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audits")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<AuditResultDto> analyze(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody AuditRequest request) {
        return ResponseEntity.ok(auditService.analyze(user.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<Page<AuditSummaryDto>> getHistory(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(auditService.getHistory(user.getUsername(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditResultDto> getById(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String id) {
        return ResponseEntity.ok(auditService.getById(user.getUsername(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String id) {
        auditService.deleteById(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}

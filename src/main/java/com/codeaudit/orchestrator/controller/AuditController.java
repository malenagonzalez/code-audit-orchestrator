package com.codeaudit.orchestrator.controller;

import com.codeaudit.orchestrator.dto.request.AuditRequest;
import com.codeaudit.orchestrator.dto.response.AuditResultDto;
import com.codeaudit.orchestrator.dto.response.AuditSummaryDto;
import com.codeaudit.orchestrator.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audits")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AuditResultDto submit(
            @Valid @RequestBody AuditRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return auditService.submit(userDetails.getUsername(), request);
    }

    @GetMapping
    public Page<AuditSummaryDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return auditService.listByUser(userDetails.getUsername(), PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public AuditResultDto findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return auditService.getById(userDetails.getUsername(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        auditService.deleteById(userDetails.getUsername(), id);
    }
}

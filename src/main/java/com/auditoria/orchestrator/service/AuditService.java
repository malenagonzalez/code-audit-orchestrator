package com.auditoria.orchestrator.service;

import com.auditoria.orchestrator.dto.request.AuditRequest;
import com.auditoria.orchestrator.dto.response.*;
import com.auditoria.orchestrator.entity.*;
import com.auditoria.orchestrator.exception.NotFoundException;
import com.auditoria.orchestrator.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRecordRepository auditRecordRepository;
    private final UserRepository userRepository;
    private final AiInferenceClient aiInferenceClient;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional
    public AuditResultDto analyze(String userEmail, AuditRequest request) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        var record = AuditRecord.builder()
                .user(user)
                .codeSnippet(request.code())
                .language(request.language())
                .status("PROCESSING")
                .build();
        auditRecordRepository.save(record);

        try {
            var aiResponse = aiInferenceClient.analyze(request);

            List<AuditIssue> issues = aiResponse.issues().stream()
                    .map(ai -> AuditIssue.builder()
                            .auditRecord(record)
                            .severity(ai.severity())
                            .category(ai.category())
                            .title(ai.title())
                            .description(ai.description())
                            .lineStart(ai.lineStart())
                            .lineEnd(ai.lineEnd())
                            .refactoredCode(ai.refactoredCode())
                            .build())
                    .toList();

            record.getIssues().addAll(issues);
            record.setPedagogicalExplanation(aiResponse.pedagogicalExplanation());
            record.setStatus("COMPLETED");
            record.setCompletedAt(LocalDateTime.now());
            auditRecordRepository.save(record);

            return toResultDto(record);
        } catch (Exception e) {
            record.setStatus("FAILED");
            auditRecordRepository.save(record);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditSummaryDto> getHistory(String userEmail, Pageable pageable) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return auditRecordRepository.findByUserId(user.getId(), pageable)
                .map(this::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public AuditResultDto getById(String userEmail, String auditId) {
        var record = auditRecordRepository.findById(auditId)
                .orElseThrow(() -> new NotFoundException("Auditoría no encontrada"));
        if (!record.getUser().getEmail().equals(userEmail)) {
            throw new NotFoundException("Auditoría no encontrada");
        }
        return toResultDto(record);
    }

    @Transactional
    public void deleteById(String userEmail, String auditId) {
        var record = auditRecordRepository.findById(auditId)
                .orElseThrow(() -> new NotFoundException("Auditoría no encontrada"));
        if (!record.getUser().getEmail().equals(userEmail)) {
            throw new NotFoundException("Auditoría no encontrada");
        }
        auditRecordRepository.delete(record);
    }

    private AuditResultDto toResultDto(AuditRecord r) {
        var issues = r.getIssues().stream().map(i -> new AuditIssueDto(
                i.getId(), i.getSeverity(), i.getCategory(),
                i.getTitle(), i.getDescription(),
                i.getLineStart(), i.getLineEnd(), i.getRefactoredCode()
        )).toList();

        return new AuditResultDto(
                r.getId(), r.getLanguage(), r.getStatus(),
                r.getPedagogicalExplanation(),
                r.getCreatedAt().format(FMT),
                r.getCompletedAt() != null ? r.getCompletedAt().format(FMT) : null,
                issues
        );
    }

    private AuditSummaryDto toSummaryDto(AuditRecord r) {
        long criticals = r.getIssues().stream()
                .filter(i -> "CRITICO".equals(i.getSeverity())).count();
        String snippet = r.getCodeSnippet().length() > 200
                ? r.getCodeSnippet().substring(0, 200) + "..."
                : r.getCodeSnippet();
        return new AuditSummaryDto(
                r.getId(), r.getLanguage(), r.getStatus(),
                snippet, r.getIssues().size(), criticals,
                r.getCreatedAt().format(FMT)
        );
    }
}

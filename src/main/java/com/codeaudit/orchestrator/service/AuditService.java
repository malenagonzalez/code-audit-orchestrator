package com.codeaudit.orchestrator.service;

import com.codeaudit.orchestrator.dto.request.AuditRequest;
import com.codeaudit.orchestrator.dto.response.AuditIssueDto;
import com.codeaudit.orchestrator.dto.response.AuditResultDto;
import com.codeaudit.orchestrator.dto.response.AuditSummaryDto;
import com.codeaudit.orchestrator.entity.AuditIssue;
import com.codeaudit.orchestrator.entity.AuditRecord;
import com.codeaudit.orchestrator.entity.User;
import com.codeaudit.orchestrator.exception.NotFoundException;
import com.codeaudit.orchestrator.repository.AuditRecordRepository;
import com.codeaudit.orchestrator.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditService {

    private final AuditRecordRepository auditRecordRepository;
    private final UserRepository userRepository;
    private final AiInferenceClient aiInferenceClient;

    public AuditResultDto submit(String userEmail, AuditRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        AuditRecord draft = AuditRecord.builder()
                .user(user)
                .code(request.code())
                .language(request.language())
                .status("PENDING")
                .build();
        final AuditRecord record = auditRecordRepository.save(draft);

        try {
            AiInferenceClient.AiResponse aiResponse = aiInferenceClient.analyze(request);

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

            record.setStatus("COMPLETED");
            record.setPedagogicalExplanation(aiResponse.pedagogicalExplanation());
            record.setCompletedAt(LocalDateTime.now());
            record.getIssues().addAll(issues);
            auditRecordRepository.save(record);

        } catch (Exception ex) {
            record.setStatus("FAILED");
            auditRecordRepository.save(record);
            throw ex;
        }

        return toResultDto(record);
    }

    @Transactional
    public Page<AuditSummaryDto> listByUser(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return auditRecordRepository.findByUserId(user.getId(), pageable)
                .map(this::toSummaryDto);
    }

    @Transactional
    public AuditResultDto getById(String userEmail, Long id) {
        AuditRecord record = auditRecordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Auditoría no encontrada"));
        if (!record.getUser().getEmail().equals(userEmail)) {
            throw new NotFoundException("Auditoría no encontrada");
        }
        return toResultDto(record);
    }

    public void deleteById(String userEmail, Long id) {
        AuditRecord record = auditRecordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Auditoría no encontrada"));
        if (!record.getUser().getEmail().equals(userEmail)) {
            throw new NotFoundException("Auditoría no encontrada");
        }
        auditRecordRepository.delete(record);
    }

    private AuditResultDto toResultDto(AuditRecord r) {
        List<AuditIssueDto> issues = r.getIssues().stream()
                .map(i -> new AuditIssueDto(i.getId(), i.getSeverity(), i.getCategory(),
                        i.getTitle(), i.getDescription(), i.getLineStart(), i.getLineEnd(),
                        i.getRefactoredCode()))
                .toList();
        return new AuditResultDto(
                r.getId(), r.getLanguage(), r.getStatus(),
                r.getPedagogicalExplanation(),
                r.getCreatedAt() != null ? r.getCreatedAt().toString() : null,
                r.getCompletedAt() != null ? r.getCompletedAt().toString() : null,
                issues);
    }

    private AuditSummaryDto toSummaryDto(AuditRecord r) {
        String snippet = r.getCode() != null && r.getCode().length() > 120
                ? r.getCode().substring(0, 120) + "..."
                : r.getCode();
        long criticals = r.getIssues().stream()
                .filter(i -> "CRITICAL".equalsIgnoreCase(i.getSeverity()))
                .count();
        return new AuditSummaryDto(
                r.getId(), r.getLanguage(), r.getStatus(),
                snippet, r.getIssues().size(), criticals,
                r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
    }
}

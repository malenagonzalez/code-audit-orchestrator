package com.auditoria.orchestrator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "audit_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditRecord {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "code_snippet", nullable = false, columnDefinition = "TEXT")
    private String codeSnippet;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "pedagogical_explanation", columnDefinition = "TEXT")
    private String pedagogicalExplanation;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "auditRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AuditIssue> issues = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}

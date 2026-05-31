package com.auditoria.orchestrator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "audit_issues")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditIssue {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_record_id", nullable = false)
    private AuditRecord auditRecord;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "line_start")
    private Integer lineStart;

    @Column(name = "line_end")
    private Integer lineEnd;

    @Column(name = "refactored_code", columnDefinition = "TEXT")
    private String refactoredCode;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}

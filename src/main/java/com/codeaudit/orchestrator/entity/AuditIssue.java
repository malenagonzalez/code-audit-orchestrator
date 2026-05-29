package com.codeaudit.orchestrator.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_issues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "audit_record_id", nullable = false)
    private AuditRecord auditRecord;

    private String severity;
    private String category;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "line_start")
    private Integer lineStart;

    @Column(name = "line_end")
    private Integer lineEnd;

    @Column(name = "refactored_code", columnDefinition = "TEXT")
    private String refactoredCode;
}

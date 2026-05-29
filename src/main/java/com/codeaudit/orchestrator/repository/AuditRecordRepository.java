package com.codeaudit.orchestrator.repository;

import com.codeaudit.orchestrator.entity.AuditRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {
    Page<AuditRecord> findByUserId(Long userId, Pageable pageable);
}

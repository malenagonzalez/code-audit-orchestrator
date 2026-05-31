package com.auditoria.orchestrator.repository;

import com.auditoria.orchestrator.entity.AuditRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, String> {
    Page<AuditRecord> findByUserId(String userId, Pageable pageable);
}

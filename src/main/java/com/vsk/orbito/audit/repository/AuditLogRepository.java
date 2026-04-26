package com.vsk.orbito.audit.repository;

import com.vsk.orbito.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUserEmailOrderByCreatedAtDesc(
            String userEmail, Pageable pageable);

    Page<AuditLog> findByStatusOrderByCreatedAtDesc(
            String status, Pageable pageable);

    List<AuditLog> findByCreatedAtBetween(
            LocalDateTime from, LocalDateTime to);

    long countByStatus(String status);
}
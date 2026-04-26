package com.vsk.orbito.audit.controller;

import com.vsk.orbito.audit.entity.AuditLog;
import com.vsk.orbito.audit.repository.AuditLogRepository;
import com.vsk.orbito.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit logs — ADMIN only")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all audit logs — ADMIN only")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AuditLog> logs = auditLogRepository.findAll(
                PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "createdAt")));

        return ResponseEntity.ok(
                ApiResponse.success("Audit logs fetched", logs));
    }

    @GetMapping("/user/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs for a specific user")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getByUser(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AuditLog> logs = auditLogRepository
                .findByUserEmailOrderByCreatedAtDesc(
                        email,
                        PageRequest.of(page, size));

        return ResponseEntity.ok(
                ApiResponse.success("User audit logs fetched", logs));
    }

    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all failed API calls")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getFailed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AuditLog> logs = auditLogRepository
                .findByStatusOrderByCreatedAtDesc(
                        "FAILED",
                        PageRequest.of(page, size));

        return ResponseEntity.ok(
                ApiResponse.success("Failed calls fetched", logs));
    }
}
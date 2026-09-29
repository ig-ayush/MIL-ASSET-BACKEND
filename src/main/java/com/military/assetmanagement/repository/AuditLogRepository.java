package com.military.assetmanagement.repository;

import com.military.assetmanagement.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLog> findByBaseIdOrderByCreatedAtDesc(Long baseId, Pageable pageable);

    List<AuditLog> findTop20ByOrderByCreatedAtDesc();
}

package com.military.assetmanagement.audit;

import com.military.assetmanagement.entity.AuditLog;
import com.military.assetmanagement.entity.Base;
import com.military.assetmanagement.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String entityType, Long entityId, Base base,
                    Long userId, String userEmail, String description) {
        log(action, entityType, entityId, base, userId, userEmail, description, null, null);
    }

    public void log(String action, String entityType, Long entityId, Base base,
                    Long userId, String userEmail, String description,
                    String oldValue, String newValue) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .base(base)
                    .userId(userId)
                    .userEmail(userEmail)
                    .description(description)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("AuditLog recorded: action={}, entityType={}, entityId={}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }
}

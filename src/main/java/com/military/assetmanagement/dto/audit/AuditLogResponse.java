package com.military.assetmanagement.dto.audit;

import com.military.assetmanagement.entity.AuditLog;
import java.time.LocalDateTime;

public record AuditLogResponse(
    Long id,
    String action,
    String entityType,
    Long entityId,
    Long baseId,
    String baseName,
    Long userId,
    String userEmail,
    String description,
    String oldValue,
    String newValue,
    LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
            log.getId(), log.getAction(), log.getEntityType(), log.getEntityId(),
            log.getBase() != null ? log.getBase().getId() : null,
            log.getBase() != null ? log.getBase().getName() : null,
            log.getUserId(), log.getUserEmail(),
            log.getDescription(), log.getOldValue(), log.getNewValue(),
            log.getCreatedAt()
        );
    }
}

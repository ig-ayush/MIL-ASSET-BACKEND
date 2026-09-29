package com.military.assetmanagement.dto.assignment;

import com.military.assetmanagement.entity.Assignment;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssignmentResponse(
    Long id,
    Long baseId,
    String baseName,
    Long equipmentTypeId,
    String equipmentTypeName,
    int quantity,
    String assignedTo,
    String purpose,
    LocalDate assignmentDate,
    LocalDate returnDate,
    String notes,
    Long createdById,
    String createdByName,
    LocalDateTime createdAt
) {
    public static AssignmentResponse from(Assignment a) {
        return new AssignmentResponse(
            a.getId(),
            a.getBase() != null ? a.getBase().getId() : null,
            a.getBase() != null ? a.getBase().getName() : null,
            a.getEquipmentType() != null ? a.getEquipmentType().getId() : null,
            a.getEquipmentType() != null ? a.getEquipmentType().getName() : null,
            a.getQuantity(),
            a.getAssignedTo(),
            a.getPurpose(),
            a.getAssignmentDate(),
            a.getReturnDate(),
            a.getNotes(),
            a.getCreatedBy() != null ? a.getCreatedBy().getId() : null,
            a.getCreatedBy() != null ? a.getCreatedBy().getName() : null,
            a.getCreatedAt()
        );
    }
}

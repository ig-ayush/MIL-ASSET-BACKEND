package com.military.assetmanagement.dto.expenditure;

import com.military.assetmanagement.entity.Expenditure;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenditureResponse(
    Long id,
    Long baseId,
    String baseName,
    Long equipmentTypeId,
    String equipmentTypeName,
    int quantity,
    String reason,
    LocalDate expenditureDate,
    String notes,
    Long createdById,
    String createdByName,
    LocalDateTime createdAt
) {
    public static ExpenditureResponse from(Expenditure e) {
        return new ExpenditureResponse(
            e.getId(),
            e.getBase() != null ? e.getBase().getId() : null,
            e.getBase() != null ? e.getBase().getName() : null,
            e.getEquipmentType() != null ? e.getEquipmentType().getId() : null,
            e.getEquipmentType() != null ? e.getEquipmentType().getName() : null,
            e.getQuantity(),
            e.getReason(),
            e.getExpenditureDate(),
            e.getNotes(),
            e.getCreatedBy() != null ? e.getCreatedBy().getId() : null,
            e.getCreatedBy() != null ? e.getCreatedBy().getName() : null,
            e.getCreatedAt()
        );
    }
}

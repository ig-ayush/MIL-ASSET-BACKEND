package com.military.assetmanagement.dto.transfer;

import com.military.assetmanagement.entity.Transfer;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransferResponse(
    Long id,
    Long sourceBaseId,
    String sourceBaseName,
    Long destinationBaseId,
    String destinationBaseName,
    Long equipmentTypeId,
    String equipmentTypeName,
    int quantity,
    LocalDate transferDate,
    String reason,
    String status,
    String notes,
    Long createdById,
    String createdByName,
    LocalDateTime createdAt
) {
    public static TransferResponse from(Transfer t) {
        return new TransferResponse(
            t.getId(),
            t.getSourceBase() != null ? t.getSourceBase().getId() : null,
            t.getSourceBase() != null ? t.getSourceBase().getName() : null,
            t.getDestinationBase() != null ? t.getDestinationBase().getId() : null,
            t.getDestinationBase() != null ? t.getDestinationBase().getName() : null,
            t.getEquipmentType() != null ? t.getEquipmentType().getId() : null,
            t.getEquipmentType() != null ? t.getEquipmentType().getName() : null,
            t.getQuantity(),
            t.getTransferDate(),
            t.getReason(),
            t.getStatus() != null ? t.getStatus().name() : null,
            t.getNotes(),
            t.getCreatedBy() != null ? t.getCreatedBy().getId() : null,
            t.getCreatedBy() != null ? t.getCreatedBy().getName() : null,
            t.getCreatedAt()
        );
    }
}

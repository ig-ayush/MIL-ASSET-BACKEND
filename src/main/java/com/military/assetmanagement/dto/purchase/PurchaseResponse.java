package com.military.assetmanagement.dto.purchase;

import com.military.assetmanagement.entity.Purchase;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PurchaseResponse(
    Long id,
    Long baseId,
    String baseName,
    Long equipmentTypeId,
    String equipmentTypeName,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice,
    String supplier,
    LocalDate purchaseDate,
    String notes,
    Long createdById,
    String createdByName,
    LocalDateTime createdAt
) {
    public static PurchaseResponse from(Purchase p) {
        return new PurchaseResponse(
            p.getId(),
            p.getBase() != null ? p.getBase().getId() : null,
            p.getBase() != null ? p.getBase().getName() : null,
            p.getEquipmentType() != null ? p.getEquipmentType().getId() : null,
            p.getEquipmentType() != null ? p.getEquipmentType().getName() : null,
            p.getQuantity(),
            p.getUnitPrice(),
            p.getTotalPrice(),
            p.getSupplier(),
            p.getPurchaseDate(),
            p.getNotes(),
            p.getCreatedBy() != null ? p.getCreatedBy().getId() : null,
            p.getCreatedBy() != null ? p.getCreatedBy().getName() : null,
            p.getCreatedAt()
        );
    }
}

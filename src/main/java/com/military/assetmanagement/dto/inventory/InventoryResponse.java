package com.military.assetmanagement.dto.inventory;

import com.military.assetmanagement.entity.AssetInventory;

public record InventoryResponse(
    Long id,
    Long baseId,
    String baseName,
    Long equipmentTypeId,
    String equipmentTypeName,
    String unit,
    int openingBalance,
    int currentQuantity,
    int assignedQuantity,
    int expendedQuantity
) {
    public static InventoryResponse from(AssetInventory inv) {
        return new InventoryResponse(
            inv.getId(),
            inv.getBase().getId(), inv.getBase().getName(),
            inv.getEquipmentType().getId(), inv.getEquipmentType().getName(), inv.getEquipmentType().getUnit(),
            inv.getOpeningBalance(), inv.getCurrentQuantity(), inv.getAssignedQuantity(), inv.getExpendedQuantity()
        );
    }
}

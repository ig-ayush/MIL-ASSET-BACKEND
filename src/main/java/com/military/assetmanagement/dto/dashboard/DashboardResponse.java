package com.military.assetmanagement.dto.dashboard;

import java.util.List;

public record DashboardResponse(
    Long baseId,
    String baseName,
    int openingBalance,
    int totalPurchases,
    int totalTransferIn,
    int totalTransferOut,
    int closingBalance,
    int totalAssigned,
    int totalExpended,
    int netMovement,
    List<EquipmentSummary> equipmentSummaries
) {
    public record EquipmentSummary(
        Long equipmentTypeId,
        String equipmentTypeName,
        String unit,
        int openingBalance,
        int currentQuantity,
        int assigned,
        int expended
    ) {}
}

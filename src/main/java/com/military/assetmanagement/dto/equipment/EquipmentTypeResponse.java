package com.military.assetmanagement.dto.equipment;

import com.military.assetmanagement.entity.EquipmentType;
import java.time.LocalDateTime;

public record EquipmentTypeResponse(
    Long id,
    String name,
    String description,
    String unit,
    boolean active,
    LocalDateTime createdAt
) {
    public static EquipmentTypeResponse from(EquipmentType et) {
        return new EquipmentTypeResponse(et.getId(), et.getName(), et.getDescription(),
            et.getUnit(), et.isActive(), et.getCreatedAt());
    }
}

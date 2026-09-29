package com.military.assetmanagement.dto.base;

import com.military.assetmanagement.entity.Base;
import java.time.LocalDateTime;

public record BaseResponse(
    Long id,
    String name,
    String location,
    String description,
    boolean active,
    LocalDateTime createdAt
) {
    public static BaseResponse from(Base base) {
        return new BaseResponse(base.getId(), base.getName(), base.getLocation(),
            base.getDescription(), base.isActive(), base.getCreatedAt());
    }
}

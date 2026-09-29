package com.military.assetmanagement.dto.user;

import com.military.assetmanagement.entity.User;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String name,
    String email,
    String role,
    Long baseId,
    String baseName,
    boolean active,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(), user.getName(), user.getEmail(),
            user.getRole().name(),
            user.getBase() != null ? user.getBase().getId() : null,
            user.getBase() != null ? user.getBase().getName() : null,
            user.isActive(), user.getCreatedAt()
        );
    }
}

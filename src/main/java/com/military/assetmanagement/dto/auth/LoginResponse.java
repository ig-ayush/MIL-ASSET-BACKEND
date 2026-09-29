package com.military.assetmanagement.dto.auth;

public record LoginResponse(
    String token,
    String type,
    Long userId,
    String name,
    String email,
    String role,
    Long baseId,
    String baseName
) {
    public static LoginResponse of(String token, Long userId, String name, String email, String role, Long baseId, String baseName) {
        return new LoginResponse(token, "Bearer", userId, name, email, role, baseId, baseName);
    }
}

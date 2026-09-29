package com.military.assetmanagement.dto.user;

import jakarta.validation.constraints.*;

public record UserRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Email @Size(max = 150) String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String role,
    Long baseId
) {}

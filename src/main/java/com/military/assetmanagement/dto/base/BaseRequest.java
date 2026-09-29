package com.military.assetmanagement.dto.base;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BaseRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 200) String location,
    @Size(max = 500) String description
) {}

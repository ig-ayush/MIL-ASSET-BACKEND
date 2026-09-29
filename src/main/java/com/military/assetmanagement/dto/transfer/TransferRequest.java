package com.military.assetmanagement.dto.transfer;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record TransferRequest(
    @NotNull Long sourceBaseId,
    @NotNull Long destinationBaseId,
    @NotNull Long equipmentTypeId,
    @NotNull @Min(1) Integer quantity,
    @NotNull LocalDate transferDate,
    String reason,
    String notes
) {}

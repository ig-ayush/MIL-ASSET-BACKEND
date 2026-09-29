package com.military.assetmanagement.dto.expenditure;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ExpenditureRequest(
    @NotNull Long baseId,
    @NotNull Long equipmentTypeId,
    @NotNull @Min(1) Integer quantity,
    @NotBlank @Size(max = 500) String reason,
    @NotNull LocalDate expenditureDate,
    String notes
) {}

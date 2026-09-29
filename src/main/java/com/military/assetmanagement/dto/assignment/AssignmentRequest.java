package com.military.assetmanagement.dto.assignment;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AssignmentRequest(
    @NotNull Long baseId,
    @NotNull Long equipmentTypeId,
    @NotNull @Min(1) Integer quantity,
    @NotBlank @Size(max = 200) String assignedTo,
    @Size(max = 500) String purpose,
    @NotNull LocalDate assignmentDate,
    LocalDate returnDate,
    String notes
) {}

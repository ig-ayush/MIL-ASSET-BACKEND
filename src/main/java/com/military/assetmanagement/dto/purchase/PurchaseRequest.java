package com.military.assetmanagement.dto.purchase;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequest(
    @NotNull Long baseId,
    @NotNull Long equipmentTypeId,
    @NotNull @Min(1) Integer quantity,
    BigDecimal unitPrice,
    String supplier,
    @NotNull LocalDate purchaseDate,
    String notes
) {}

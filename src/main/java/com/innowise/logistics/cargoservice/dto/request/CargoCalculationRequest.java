package com.innowise.logistics.cargoservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CargoCalculationRequest(
        @NotNull(message = "ID товара не может быть пустым")
        Long cargoId,

        @NotNull(message = "Количество не может быть пустым")
        @Min(value = 1, message = "Количество товара должно быть не менее 1")
        Integer quantity
) {}

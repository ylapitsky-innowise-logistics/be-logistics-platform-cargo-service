package com.innowise.logistics.cargoservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CargoReservationRequest {
    @NotNull(message = "ID артикула товара не может быть пустым")
    Long skuId;

    @NotNull(message = "Количество товаров не может быть пустым")
    @Min(value = 1, message = "Количество товаров должно быть не менее 1")
    Integer quantity;
}

/*

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CargoReservationRequest (
    @NotNull(message = "ID артикула товара не может быть пустым")
    Long skuId,

    @NotNull(message = "Количество товаров не может быть пустым")
    @Min(value = 1, message = "Количество товаров должно быть не менее 1")
    Integer quantity
) {}
*/

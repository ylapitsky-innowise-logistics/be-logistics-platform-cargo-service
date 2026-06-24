package com.innowise.logistics.cargoservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Запрос на создание нового габаритного размера.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DimensionCreatingRequest {

    @NotNull(message = "Длина (length) является обязательным полем")
    @Positive(message = "Длина товара должна быть больше нуля")
    private Double length;

    @NotNull(message = "Ширина (width) является обязательным полем")
    @Positive(message = "Ширина товара должна быть больше нуля")
    private Double width;

    @NotNull(message = "Высота (height) является обязательным полем")
    @Positive(message = "Высота товара должна быть больше нуля")
    private Double height;
}

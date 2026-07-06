package com.innowise.logistics.cargoservice.dto.request;

import com.innowise.logistics.cargoservice.entity.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * DTO-запрос на создание новой единицы груза (товара) на складе.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CargoCreatingRequest {

    @NotNull(message = "ID артикула (skuId) является обязательным полем")
    private Long skuId;

    @NotBlank(message = "Наименование товара не может быть пустым")
    private String name;

    @NotNull(message = "Категория товара является обязательным полем")
    private Category category;

    @NotNull(message = "Вес товара является обязательным полем")
    @Positive(message = "Вес товара должен быть больше нуля")
    private Double weight; // в килограммах

    @NotNull(message = "ID габаритных размеров (dimensionId) является обязательным полем")
    private Long dimensionId;

    @NotNull(message = "Стоимость товара является обязательным полем")
    @DecimalMin(value = "0.00", message = "Стоимость товара не может быть отрицательной")
    private BigDecimal price;

    @NotNull(message = "ID складской ячейки (locationId) является обязательным полем")
    private Long locationId;
}

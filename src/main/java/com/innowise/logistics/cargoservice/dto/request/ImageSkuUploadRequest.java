package com.innowise.logistics.cargoservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ImageSkuUploadRequest {

    @NotNull(message = "ID артикула (skuId) обязателен")
    private Long skuId;

    private String description = "";

    @Min(value = 0, message = "Порядок сортировки не может быть отрицательным")
    private Integer sortOrder = 0;

    private Boolean isPrimary = false;
}

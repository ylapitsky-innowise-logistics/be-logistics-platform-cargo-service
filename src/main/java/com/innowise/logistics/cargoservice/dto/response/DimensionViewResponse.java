package com.innowise.logistics.cargoservice.dto.response;

/**
 * DTO-ответ для отображения информации о габаритах товара.
 */
public record DimensionViewResponse(
        Long id,
        Double length,
        Double width,
        Double height
) {
}

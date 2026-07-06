package com.innowise.logistics.cargoservice.dto.response;

/**
 * DTO-ответ, возвращающий ID успешно созданного каталожного артикула (SKU).
 */
public record SkuCreatingResponse(
        Long id
) {
}

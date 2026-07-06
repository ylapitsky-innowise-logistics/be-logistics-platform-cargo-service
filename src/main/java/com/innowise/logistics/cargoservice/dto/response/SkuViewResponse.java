package com.innowise.logistics.cargoservice.dto.response;

import java.time.Instant;

/**
 * DTO-ответ для отображения полной детальной информации об артикуле (SKU).
 */
public record SkuViewResponse(
        Long id,
        String name,
        String description,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}

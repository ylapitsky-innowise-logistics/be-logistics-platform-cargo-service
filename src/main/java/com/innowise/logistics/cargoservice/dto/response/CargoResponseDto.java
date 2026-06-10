package com.innowise.logistics.cargoservice.dto.response;

import com.innowise.logistics.cargoservice.entity.Category;
import com.innowise.logistics.cargoservice.entity.Status;
import java.math.BigDecimal;
import java.time.Instant;

public record CargoResponseDto(
        Long id,
        String skuName,          // Берем из связанной сущности Sku
        String mongoDocId,
        String name,
        Category category,
        Double weight,
        String dimensions,       // Форматируем красиво: "LxWxH"
        BigDecimal price,
        String locationLocation, // Собираем как "Стеллаж / Полка"
        Instant createdAt,
        Status status,
        Instant statusAt
) {}

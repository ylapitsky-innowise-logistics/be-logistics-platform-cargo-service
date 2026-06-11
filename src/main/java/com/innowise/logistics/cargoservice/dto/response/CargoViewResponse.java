package com.innowise.logistics.cargoservice.dto.response;

import com.innowise.logistics.cargoservice.entity.Category;
import com.innowise.logistics.cargoservice.entity.Status;
import java.math.BigDecimal;

public record CargoViewResponse(
        Long id,
        Long skuId,          // Берем из связанной сущности Sku
        String skuName,          // Берем из связанной сущности Sku
        String mongoDocId,
        String name,
        Category category,
        String weight,
        String dimensions,       // Форматируем красиво: "LxWxH"
        BigDecimal price,
        String location, // Собираем как "Стеллаж / Полка"
        String createdAt,
        Status status,
        String statusAt
) {}

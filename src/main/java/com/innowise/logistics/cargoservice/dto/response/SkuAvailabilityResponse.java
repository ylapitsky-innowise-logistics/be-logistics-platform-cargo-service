package com.innowise.logistics.cargoservice.dto.response;

import com.innowise.logistics.cargoservice.entity.Category;
import com.innowise.logistics.cargoservice.entity.Dimension;
import com.innowise.logistics.cargoservice.entity.Sku;

import java.math.BigDecimal;
import java.time.Instant;

public record SkuAvailabilityResponse(
        // Поля по результатам выборки
        Sku sku,
        String mongoDocId,                          // Линк на MongoDB
        String name,
        Category category,
        Double weight,
        Dimension dimension,

        // Вычисляемые поля
        BigDecimal priceMin,
        BigDecimal priceMax,
        Instant createdAtFrom,
        Instant createdAtTo,
        Long availableQuantity     // сколько таких товаров доступно
) {}
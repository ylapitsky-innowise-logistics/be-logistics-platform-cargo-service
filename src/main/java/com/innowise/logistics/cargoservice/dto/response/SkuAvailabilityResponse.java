package com.innowise.logistics.cargoservice.dto.response;

import com.innowise.logistics.cargoservice.entity.Category;
import com.innowise.logistics.cargoservice.entity.Dimension;

import java.math.BigDecimal;

public record SkuAvailabilityResponse(
        Long skuId,
        String skuName,

        String mongoDocId,                          // Линк на MongoDB
        String name,
        Category category,
        Double weight,
        Dimension dimension,
        BigDecimal price,

        Long availableQuantity     // сколько товаров этого SKU доступно
) {}
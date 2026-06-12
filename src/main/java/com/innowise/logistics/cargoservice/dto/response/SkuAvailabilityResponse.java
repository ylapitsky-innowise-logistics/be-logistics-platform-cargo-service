package com.innowise.logistics.cargoservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.innowise.logistics.cargoservice.entity.Category;
import com.innowise.logistics.cargoservice.entity.Dimension;
import com.innowise.logistics.cargoservice.entity.Sku;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
// 🟢 Форматируем дату и принудительно переводим Instant (UTC) в зону GMT+3
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm 'GMT+3'", timezone = "GMT+3")
        Instant createdAtFrom,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm 'GMT+3'", timezone = "GMT+3")
        Instant createdAtTo,        Long availableQuantity     // сколько таких товаров доступно
) {
    /**
     * 🟢 Компактный конструктор Java Record.
     * Он автоматически перехватывает создание объекта (включая JPQL оператор 'new')
     * и математически округляет вес до сотых.
     */
    public SkuAvailabilityResponse {
        if (weight != null) {
            weight = BigDecimal.valueOf(weight)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
    }
}
package com.innowise.logistics.cargoservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.innowise.logistics.cargoservice.entity.Category;
import com.innowise.logistics.cargoservice.entity.Dimension;
import com.innowise.logistics.cargoservice.entity.Sku;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public record SkuAvailabilityResponse(

        // Информация о SKU (каталог)
        Sku sku,
        String name,                    // из Cargo
        Category category,              // из Cargo
        Double weight,                  // из Cargo
        Dimension dimension,            // из Cargo

        // Статистика по ценам / Вычисляемые поля
        BigDecimal priceMin,
        BigDecimal priceMax,

        // Даты
        // 🟢 Форматируем дату и принудительно переводим Instant (UTC) в зону GMT+3
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm 'GMT+3'", timezone = "GMT+3")
        Instant createdAtFrom,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm 'GMT+3'", timezone = "GMT+3")
        Instant createdAtTo,

        // Количество доступных товаров
        Long availableQuantity     // сколько таких товаров доступно
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
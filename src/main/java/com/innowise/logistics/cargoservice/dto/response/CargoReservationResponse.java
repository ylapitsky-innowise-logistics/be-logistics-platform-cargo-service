package com.innowise.logistics.cargoservice.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record CargoReservationResponse(
//        Long bookingId,           // Id брони
//        Instant createdAt,        // Дата создания брони
//
//        BigDecimal totalPrice,    // Итоговая сумма стоимости всех позиций
//        Double totalWeight,       // Итоговый вес
//        Integer totalItemsCount,  // Общее количество единиц товара
//        String currency           // Валюта


// ===== 1. Идентификаторы и Аудит =====
        Long reservationId,         // Id брони
        Instant createdAt,          // Дата создания брони
        Boolean isActive,           // статус брони из Entity

        // ===== 2. Содержимое брони =====
        Set<Long> cargoIds,         // Добавили Set ID -шников. Имя совпадает с Entity для легкого маппинга

        // ===== 3. Агрегированные физические метрики =====
        Integer totalQuantity,      // Общее количество единиц товара
        Double totalWeight,         // Итоговый вес

        // ===== 4. Финансовый блок (Деньги и валюта всегда идут парой!) =====
        BigDecimal totalPrice,      // Итоговая сумма стоимости всех позиций
        String currency             // Валюта
) {}

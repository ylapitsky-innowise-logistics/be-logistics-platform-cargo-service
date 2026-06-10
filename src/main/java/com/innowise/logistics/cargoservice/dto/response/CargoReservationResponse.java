package com.innowise.logistics.cargoservice.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record CargoReservationResponse(
        Long bookingId,           // Id брони
        Instant createdAt,        // Дата создания брони

        BigDecimal totalPrice,    // Итоговая сумма стоимости всех позиций
        Double totalWeight,       // Итоговый вес
        Integer totalItemsCount,  // Общее количество единиц товара
        String currency           // Валюта
) {}

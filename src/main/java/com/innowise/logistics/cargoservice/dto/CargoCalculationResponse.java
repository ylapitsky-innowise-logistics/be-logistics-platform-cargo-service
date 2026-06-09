package com.innowise.logistics.cargoservice.dto;

import java.math.BigDecimal;

public record CargoCalculationResponse(
        BigDecimal totalPrice,    // Итоговая сумма стоимости всех позиций
        Double totalWeight,       // Итоговый вес (полезно для логистики)
        Integer totalItemsCount,  // Общее количество единиц товара в запросе
        String currency           // Валюта (зашьем "RUB" или "USD")
) {}

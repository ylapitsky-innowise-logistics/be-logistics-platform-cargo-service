package com.innowise.logistics.cargoservice.repository.projection;

import com.innowise.logistics.cargoservice.entity.Status;

import java.math.BigDecimal;

/**
 * Задел на будущий high-load production:
 * Буду возвращать из репозитория ПРОЕКЦИЮ, т.е. набор только необходимых для резервирования полей.
 * Будем считать, что мой проект потом разрастется, поэтому целесообразно.
 */
public interface CargoReservationProjection {
    Long getId();
    BigDecimal getPrice();
    Double getWeight();
    Status getStatus();
}

// В данном пакете лежат ПРОЕКЦИИ (интерфейсы)

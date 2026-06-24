package com.innowise.logistics.cargoservice.dto.response;

/**
 * DTO-ответ, возвращающий ID успешно созданной единицы груза.
 * Реализован в виде Java Record для обеспечения неизменяемости данных.
 */
public record CargoCreatingResponse(
        Long id
) {
}
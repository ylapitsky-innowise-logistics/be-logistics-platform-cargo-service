package com.innowise.logistics.cargoservice.dto.response;

/**
 * DTO-ответ для отображения информации о ячейке хранения.
 */
public record LocationViewResponse(
        Long id,
        String rack,
        String shelf,
        Long addressId
) {
}

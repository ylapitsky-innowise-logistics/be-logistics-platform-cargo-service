package com.innowise.logistics.cargoservice.dto.response;

/**
 * DTO-ответ для отображения полной информации об адресе.
 */
public record AddressViewResponse(
        Long id,
        String country,
        String zipCode,
        String city,
        String microdistrict,
        String street,
        Integer house,
        String block,
        String apartment
) {
}

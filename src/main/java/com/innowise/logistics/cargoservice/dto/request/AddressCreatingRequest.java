package com.innowise.logistics.cargoservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Запрос на создание нового адреса (склада или клиента).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AddressCreatingRequest {

    @NotBlank(message = "Страна является обязательным полем")
    @Size(max = 50, message = "Название страны не должно превышать 50 символов")
    private String country;

    @NotBlank(message = "Почтовый индекс является обязательным полем")
    @Size(max = 10, message = "Почтовый индекс не должен превышать 10 символов")
    private String zipCode;

    @NotBlank(message = "Город является обязательным полем")
    @Size(max = 50, message = "Название города не должно превышать 50 символов")
    private String city;

    @Size(max = 100, message = "Название микрорайона не должно превышать 100 символов")
    private String microdistrict;

    @Size(max = 150, message = "Название улицы не должно превышать 150 символов")
    private String street;

    @NotNull(message = "Номер дома является обязательным полем")
    @Positive(message = "Номер дома должен быть положительным числом")
    private Integer house;

    @Size(max = 20, message = "Номер корпуса не должен превышать 20 символов")
    private String block;

    @Size(max = 20, message = "Номер квартиры/офиса не должен превышать 20 символов")
    private String apartment;
}

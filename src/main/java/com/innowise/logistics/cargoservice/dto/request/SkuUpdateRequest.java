package com.innowise.logistics.cargoservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO-запрос на изменение параметров существующего артикула.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SkuUpdateRequest {

    @NotBlank(message = "Код/название артикула (name) не может быть пустым")
    @Size(max = 100, message = "Длина названия артикула не должна превышать 100 символов")
    private String name;

    @Size(max = 1000, message = "Описание артикула не должно превышать 1000 символов")
    private String description;

    private boolean isActive;
}

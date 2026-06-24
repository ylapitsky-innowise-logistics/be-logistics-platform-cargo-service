package com.innowise.logistics.cargoservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Запрос на заведение нового каталожного артикула (SKU) в систему.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SkuCreatingRequest {

    @NotBlank(message = "Код/название артикула (name) не может быть пустым")
    @Size(max = 100, message = "Длина названия артикула не должна превышать 100 символов")
    private String name;

    @Size(max = 1000, message = "Описание артикула не должно превышать 1000 символов")
    private String description;
}

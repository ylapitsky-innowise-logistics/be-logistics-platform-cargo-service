package com.innowise.logistics.cargoservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Запрос на обновление параметров существующей складской ячейки.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LocationUpdateRequest {

    @NotBlank(message = "Координата стеллажа (rack) является обязательным полем")
    @Size(max = 50, message = "Название стеллажа не должно превышать 50 символов")
    private String rack;

    @Size(max = 50, message = "Название полки не должно превышать 50 символов")
    private String shelf;

    @NotNull(message = "ID адреса склада (addressId) является обязательным полем")
    private Long addressId;
}

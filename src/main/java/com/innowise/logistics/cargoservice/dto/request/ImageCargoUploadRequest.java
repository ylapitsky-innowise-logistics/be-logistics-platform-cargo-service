package com.innowise.logistics.cargoservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ImageCargoUploadRequest {@NotNull(message = "ID груза (cargoId) обязателен")
private Long cargoId;
    private String description = "";
    @Min(value = 0, message = "Порядок сортировки не может быть отрицательным")
    private Integer sortOrder = 0;
    private Boolean isPrimary = false;
}

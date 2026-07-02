package com.innowise.logistics.cargoservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder
public class ImageUploadRequest {

    // ID того объекта, к которому будет относиться данное изображение
    @NotNull(message = "ID является обязательным полем")
    private Long id;

    // Человеко-читаемое описание передаваемой картинки
    protected String description = "";

    @Min(value = 0, message = "Порядок сортировки не может быть отрицательным")
    protected Integer sortOrder = 0;

    // Является - ли передаваемая картинка главной?
    protected Boolean isPrimary = false;
}

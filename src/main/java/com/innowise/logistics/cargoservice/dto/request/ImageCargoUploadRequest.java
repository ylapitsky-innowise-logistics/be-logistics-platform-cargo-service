package com.innowise.logistics.cargoservice.dto.request;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
public class ImageCargoUploadRequest extends ImageUploadRequest {
    // Пустой, все поля наследуются от ImageUploadRequest
    // Можно добавить специфичные поля для Cargo, если понадобятся
}
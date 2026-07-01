package com.innowise.logistics.cargoservice.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImageCargoUploadRequest extends ImageUploadRequest {
    // Пустой, все поля наследуются от ImageUploadRequest
    // Можно добавить специфичные поля для Cargo, если понадобятся
}
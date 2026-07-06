package com.innowise.logistics.cargoservice.mongo.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.Instant;

/**
 * Базовый класс для метаданных изображений (общие поля для Cargo и Sku)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseImageMetadata {

    @Id
    private String id;


    // ===== СВЯЗЬ С ФАЙЛОМ =====
    @Indexed(unique = true)
    @Field("gridfs_file_id")
    private String gridFsFileId;


    // ===== ТЕХНИЧЕСКИЕ МЕТАДАННЫЕ ФАЙЛА =====
    @Field("file_name")
    private String fileName;           // Оригинальное имя файла

    @Field("file_size")
    private Long fileSize;             // Размер в байтах

    @Field("mime_type")
    private String mimeType;           // image/jpeg, image/png, image/webp

    @Field("width")
    private Integer width;             // Ширина в пикселях

    @Field("height")
    private Integer height;            // Высота в пикселях


    // ===== КОНТЕНТ-МЕНЕДЖМЕНТ =====
    @Field("description")
    private String description;        // Комментарий по изображению

    @Field("sort_order")
    private Integer sortOrder = 0;     // 0 — главная, 1, 2, 3 — остальные

    @Field("is_primary")
    private Boolean isPrimary = false; // Флаг главного изображения


    // ===== АУДИТ =====
    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Indexed(name = "idx_image_uploaded_at")
    @Field(value = "uploaded_at", targetType = FieldType.DATE_TIME)
    private Instant uploadedAt;
}
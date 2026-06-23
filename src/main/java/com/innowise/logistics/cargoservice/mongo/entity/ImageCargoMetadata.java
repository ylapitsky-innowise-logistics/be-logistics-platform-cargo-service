package com.innowise.logistics.cargoservice.mongo.entity;

import com.innowise.logistics.cargoservice.entity.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.Instant;

/**
 * Паспорт на картинку для самого товара (на каждую картинку)
 */
@Document(collection = "image_cargo_metadata") // 🟢 Говорим Спрингу, что это документ коллекции в MongoDB
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageCargoMetadata {

    @Id
    private String id;


    // ===== СВЯЗЬ С ФАЙЛОМ =====
    @Indexed(unique = true)
    @Field("gridfs_file_id")
    private String gridFsFileId;


    // ===== СВЯЗЬ С CARGO (бизнес-ключ) =====
    @Indexed(name = "idx_image_cargo_id")
    @Field("cargo_id")
    private Long cargoId;


    // ===== ТЕХНИЧЕСКИЕ МЕТАДАННЫЕ ФАЙЛА (новые поля!) =====
    @Field("file_name")
    private String fileName;           // Оригинальное имя файла

    @Field("file_size")
    private Long fileSize;             // Размер в байтах

    @Field("mime_type")
    private String mimeType;           // image/jpeg, image/png, image/webp

    @Field("width")
    private Integer width;             // Ширина в пикселях (опционально)

    @Field("height")
    private Integer height;            // Высота в пикселях (опционально)

    @Field("alt_text")
    private String altText;            // Alt-текст для SEO (опционально)


    // ===== БИЗНЕС-ПОЛЯ (дублируем из Cargo для независимости) =====
    @Indexed(name = "idx_image_sku_name")
    @Field("sku_name")
    private String skuName;

    @Indexed(name = "idx_image_cargo_name")
    @Field("cargo_name")
    private String cargoName;

    @Indexed(name = "idx_image_cargo_category")
    @Field("cargo_category")
    private Category cargoCategory;


    // ===== КОНТЕНТ-МЕНЕДЖМЕНТ =====
    @Field("description")
    private String description;        // Комментарий по изображению

    @Field("sort_order")
    private Integer sortOrder = 0;     // 0 — главная, 1, 2, 3 — остальные

    @Field("is_primary")
    private Boolean isPrimary = false; // Флаг главного изображения


    // ===== АУДИТ =====
    @Setter(AccessLevel.NONE)           // Отмен. Lombok сеттер.
    @CreatedDate                        // 🟢 Автоматический аудит: Spring Data Mongo сам проставит дату при save()
    @Indexed(name = "idx_image_uploaded_at")
    @Field(value = "uploaded_at", targetType = FieldType.DATE_TIME)
    private Instant uploadedAt;
}

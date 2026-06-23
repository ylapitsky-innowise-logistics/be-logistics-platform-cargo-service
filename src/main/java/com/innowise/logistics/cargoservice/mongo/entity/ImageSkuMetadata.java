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
 * Паспорт на картинку для артикула товара (на каждую картинку)
 * У одного SKU может быть МНОГО картинок (галерея)
 */
@Document(collection = "image_sku_metadata") // 🟢 Говорим Спрингу, что это документ коллекции в MongoDB
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageSkuMetadata {

    @Id                         // Системный уникальный ID документа в MongoDB
    private String id;


    // ===== СВЯЗЬ С ФАЙЛОМ =====
    @Indexed(unique = true)
    @Field("gridfs_file_id")
    private String gridFsFileId;


    // ===== СВЯЗЬ С SKU (бизнес-ключ) =====
    @Indexed(name = "idx_image_sku_id")     // 🎯 Обычный индекс для точного поиска
    @Field("sku_id")
    private Long skuId;


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


    // ===== БИЗНЕС-ПОЛЯ (дублируем из Sku для независимости) =====
    @Indexed(name = "idx_image_sku_name")
    @Field("sku_name")
    private String skuName;

    @Indexed(name = "idx_image_sku_category")
    @Field("sku_category")
    private Category category;      // категория самого артикула


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
    @Indexed(name = "idx_image_sku_uploaded_at")
    @Field(value = "uploaded_at", targetType = FieldType.DATE_TIME)
    private Instant uploadedAt;
}

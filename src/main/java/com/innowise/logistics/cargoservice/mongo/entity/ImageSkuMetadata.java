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
 * Паспорт на картинку для артикула товара
 */
@Document(collection = "image_metadata") // 🟢 Говорим Спрингу, что это документ коллекции в MongoDB
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageSkuMetadata {

    @Id                         // Системный уникальный ID документа в MongoDB
    private String id;

    @Indexed(unique = true)     // 🎯 Индекс: связь 1-к-1 с физическим файлом в GridFS
    @Field("gridfs_file_id")
    private String gridFsFileId;

    @Indexed(unique = true)     // 🎯 Обычный индекс для точного поиска / Индекс: связь 1-к-1; только одно изображение на артикул
    @Field("sku_id")            // id артикула товара, к которому относится данное изображение
    private Long skuId;

    @Indexed(name = "idx_sku_name") // Обычный индекс для точного поиска, сортировки и regex-запросов    @Field("sku_name")          // наименование артикула товара, к которому относится данное изображение
    @Field("sku_name")
    private String skuName;

    @Indexed(name = "idx_cargo_name")   // Обычный индекс для точного поиска
    @Field("cargo_name")                // наименование самого товара, к которому относится данное изображение
    private String cargoName;

    @Field("cargo_category")    // К какой категории товаров относится сам товар, отображенный на картинке
    private Category cargoCategory;

    @Field("description")       // Комментарий по данному изображению товара
    private String description;

    @Setter(AccessLevel.NONE)   // Отмен. Lombok сеттер.
    @CreatedDate                // 🟢 Автоматический аудит: Spring Data Mongo сам проставит дату при save()
    @Indexed(name = "idx_uploaded_at") // Индекс для сортировки по дате загрузки
    @Field(value = "uploaded_at", targetType = FieldType.DATE_TIME)
    private Instant uploadedAt;
}

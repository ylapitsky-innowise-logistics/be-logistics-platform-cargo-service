package com.innowise.logistics.cargoservice.mongo.entity;

import com.innowise.logistics.cargoservice.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Паспорт на картинку для самого товара (на каждую картинку)
 * У одного СCargo может быть МНОГО картинок (галерея)
 */
@Document(collection = "image_cargo_metadata") // 🟢 Говорим Спрингу, что это документ коллекции в MongoDB
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageCargoMetadata extends BaseImageMetadata {

    // ===== СВЯЗЬ С CARGO (бизнес-ключ) =====
    @Indexed(name = "idx_image_cargo_id")
    @Field("cargo_id")
    private Long cargoId;


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
}

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
 * Паспорт на картинку для артикула товара (на каждую картинку)
 * У одного SKU может быть МНОГО картинок (галерея)
 */
@Document(collection = "image_sku_metadata") // 🟢 Говорим Спрингу, что это документ коллекции в MongoDB
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageSkuMetadata extends BaseImageMetadata {

    // ===== СВЯЗЬ С SKU (бизнес-ключ) =====
    @Indexed(name = "idx_image_sku_id")     // 🎯 Обычный индекс для точного поиска
    @Field("sku_id")
    private Long skuId;


    // ===== БИЗНЕС-ПОЛЯ (дублируем из Sku для независимости) =====
    @Indexed(name = "idx_image_sku_name")
    @Field("sku_name")
    private String skuName;

    @Indexed(name = "idx_image_sku_category")
    @Field("sku_category")
    private Category category;      // категория самого артикула
}

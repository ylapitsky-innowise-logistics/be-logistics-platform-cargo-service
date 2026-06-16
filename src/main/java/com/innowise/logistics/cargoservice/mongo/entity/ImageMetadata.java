package com.innowise.logistics.cargoservice.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "image_metadata") // 🟢 Говорим Спрингу, что это документ коллекции в MongoDB
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageMetadata {

    @Id // 🟢 Системный ID документа в Mongo (String или ObjectId)
    private String id;

    @Field("gridfs_file_id") // Ссылка на физический файл, который лежит в GridFS
    private String gridFsFileId;

    @Field("cargo_id") // К какому конкретно грузу из Postgres привязана фотка
    private Long cargoId;

    @Field("uploaded_by") // Имя или ID сотрудника, сделавшего снимок
    private String uploadedBy;

    @Field("is_damaged_report") // Флаг: это обычное фото или фиксация брака/поломки?
    private Boolean isDamagedReport = false;

    @Field("description") // Комментарий кладовщика
    private String description;

    @Field("uploaded_at")
    private Instant uploadedAt = Instant.now();
}

package com.innowise.logistics.cargoservice.mongo.service;

import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Универсальный контракт сервисного слоя для управления медиа-контентом.
 * Полностью изолирован от HTTP-зависимостей и веб-аннотаций.
 *
 * @param <REQUEST> специфичный тип DTO запроса (ImageSkuUploadRequest или ImageCargoUploadRequest)
 */
public interface ImageService<REQUEST> {

    /**
     * 1️⃣ CREATE — Сохранить бинарный файл в GridFS и паспортизировать метаданные.
     */
    ImageUploadResponse uploadImage(MultipartFile file, REQUEST metadata);


    /**
     * 2️⃣ READ ALL — Получить полный список всех существующих картинок с пагинацией.
     */
    PageResponse<ImageViewResponse> getAllImages(Pageable pageable);

    /**
     * 3️⃣ READ GALLERY — Получить галерею изображений, привязанных к сущности Postgres. // READ (Get Gallery of 'ImageViewResponse' by item ID from Postgres)
     */
    PageResponse<ImageViewResponse> getGalleryByEntityId(Long entityId, Pageable pageable);

    /**
     * 4️⃣ READ ONE — Извлечь файл из GridFS для последующего стриминга.
     */
    Resource downloadImageByImageId(String fileId);


    /**
     * 5️⃣ UPDATE — Обновить исключительно метаданные описания картинки.
     */
    ImageViewResponse updateImageMetadata(String fileId, REQUEST metadata);


    /**
     * 6️⃣ DELETE ONE — Удалить бинарный файл и его метаданные по ID.
     */
    void deleteImage(String fileId);

    /**
     * 7️⃣ DELETE ALL — Каскадно очистить всю галерею для конкретного SKU или Cargo. // DELETE (All by item ID from Postgres)
     */
    void deleteImagesByEntityId(Long entityId);


    /**
     * 8️⃣ GET PRIMARY — Найти обложку (главное фото) для отображения в списках.
     */
    ImageViewResponse getPrimaryImageByEntityId(Long entityId);
}

package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** -= Интерфейс должен описывать что, реализация — как. =-
 * Базовый интерфейс для контроллеров управления изображениями.
 * Поддерживает CRUD операции с пагинацией для SKU и Cargo.
 *
 * @param <REQUEST> тип DTO для запроса загрузки/обновления (ImageSkuUploadRequest или ImageCargoUploadRequest)
 */
public interface ImageController<REQUEST> {

    /**
     * 1️⃣ CREATE — Загрузить новое изображение.
     *
     * @param file     файл изображения (multipart)
     * @param metadata метаданные изображения, JSON (связанные с SKU или Cargo)
     * @return созданный объект с ID и ссылкой на файл / ID и URL загруженного файла
     */
    ResponseEntity<ImageUploadResponse> uploadImage(MultipartFile file, REQUEST metadata);

    /**
     * 2️⃣ READ ALL — Получить все изображения из БД с пагинацией.
     *
     * @param pageable параметры пагинации и сортировки
     * @return страница с изображениями
     */
    ResponseEntity<PageResponse<ImageViewResponse>> getAllImages(Pageable pageable);

    /**
     * 3️⃣ READ GALLERY — Получить галерею изображений по ID сущности из Postgres (SKU или Cargo).
     *
     * @param entityId ID сущности из Postgres (SKU или Cargo)
     * @param pageable параметры пагинации и сортировки
     * @return страница с изображениями для указанной сущности
     */
    ResponseEntity<PageResponse<ImageViewResponse>> getGalleryByEntityId(Long entityId, Pageable pageable);

    /**
     * 4️⃣ READ ONE — Скачать конкретное изображение по ID файла этого изображения.
     *
     * @param fileId ID файла в GridFS (MongoDB ObjectId — 24 символа)
     * @return бинарный поток/ ресурс изображения
     */
    ResponseEntity<Resource> downloadImageByImageId(String fileId);

    /**
     * 5️⃣ UPDATE — Обновить метаданные изображения (описание, порядок, флаг главного).
     *
     * @param fileId   ID файла в GridFS
     * @param metadata новые метаданные
     * @return обновлённый объект с информацией об изображении
     */
    ResponseEntity<ImageViewResponse> updateImageMetadata(String fileId, REQUEST metadata);

    /**
     * 6️⃣ DELETE ONE — Удалить одно конкретное изображение по ID файла.
     *
     * @param fileId ID файла в GridFS
     * @return HTTP 204 No Content
     */
    ResponseEntity<Void> deleteImage(String fileId);

    /**
     * 7️⃣ DELETE ALL — Удалить все изображения для конкретной сущности (SKU или Cargo).
     *
     * @param entityId ID сущности (SKU или Cargo)
     * @return HTTP 204 No Content
     */
    ResponseEntity<Void> deleteImagesByEntityId(Long entityId);

    /**
     * 8️⃣ GET PRIMARY — Получить главное изображение для артикула (SKU).
     *
     * @param entityId ID сущности (SKU или Cargo)
     * @return главное изображение
     */
    ResponseEntity<ImageViewResponse> getPrimaryImageByEntityId(Long entityId);

    // =============================================
    // ===== Default методы для проверки файла =====

    //❗ПРОВЕРКА: Не пустой ли файл?
    default void isEmptyFileCheck(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не может быть пустым");
        }
    }
    //❗ПРОВЕРКА: Проверка размера (до 10 MB)
    default void isTooBigFileCheck(MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Файл не должен превышать 10 MB");
        }
    }

    //❗ПРОВЕРКА: Проверка MIME-типа
    default void isItImageFileCheck(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Допустимы только изображения");
        }
    }
}

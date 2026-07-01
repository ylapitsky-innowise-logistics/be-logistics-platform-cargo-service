package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Базовый интерфейс для контроллеров управления изображениями.
 * Поддерживает CRUD операции с пагинацией для SKU и Cargo.
 *
 * @param <REQUEST> тип DTO для запроса загрузки/обновления (ImageSkuUploadRequest или ImageCargoUploadRequest)
 */
//@RestController   - ставим на реализацию!
//@Validated        - ставим на реализацию!
public interface ImageController<REQUEST> {

    /**
     * 1️⃣ CREATE — Загрузить новое изображение.
     *
     * @param file     файл изображения
     * @param metadata метаданные изображения (связанные с SKU или Cargo)
     * @return созданный объект с ID и ссылкой на файл
     */
    ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestPart("file")
            MultipartFile file,

            @RequestPart("metadata")
            @Valid
            REQUEST metadata);

    /**
     * 2️⃣ READ ALL — Получить все изображения из БД с пагинацией.
     *
     * @param pageable параметры пагинации и сортировки
     * @return страница с изображениями
     */
    ResponseEntity<PageResponse<ImageViewResponse>> getAllImages(
            @PageableDefault(size = 5, sort = "uploadedAt")
            Pageable pageable);

    /**
     * 3️⃣ READ GALLERY — Получить галерею изображений по ID сущности из Postgres (SKU или Cargo).
     *
     * @param entityId ID сущности из Postgres (SKU или Cargo)
     * @param pageable параметры пагинации и сортировки
     * @return страница с изображениями для указанной сущности
     */
    ResponseEntity<PageResponse<ImageViewResponse>> getGalleryByEntityId(
            @PathVariable
            @Positive(message = "ID сущности должен быть положительным числом")
            Long entityId,

            @PageableDefault(size = 5, sort = "sortOrder")
            Pageable pageable);

    /**
     * 4️⃣ READ ONE — Скачать конкретное изображение по ID файла этого изображения.
     *
     * @param fileId ID файла в GridFS (MongoDB ObjectId — 24 символа)
     * @return ресурс изображения
     */
    ResponseEntity<Resource> downloadImageByImageId(
            @PathVariable
            @NotBlank(message = "ID изображения обязателен")
            @Pattern(
                    regexp = "^[a-fA-F0-9]{24}$",
                    message = "ID изображения должен быть корректным ObjectId (24 шестнадцатеричных символа)"
            )
            String fileId);

    /**
     * 5️⃣ UPDATE — Обновить метаданные изображения.
     *
     * @param fileId   ID файла в GridFS
     * @param metadata новые метаданные
     * @return обновлённый объект с информацией об изображении
     */
    ResponseEntity<ImageViewResponse> updateImageMetadata(
            @PathVariable
            @NotBlank(message = "ID изображения обязателен")
            @Pattern(
                    regexp = "^[a-fA-F0-9]{24}$",
                    message = "ID изображения должен быть корректным ObjectId (24 шестнадцатеричных символа)"
            )
            String fileId,

            @Valid
            @RequestBody
            REQUEST metadata);

    /**
     * 6️⃣ DELETE ONE — Удалить конкретное изображение по ID файла.
     *
     * @param fileId ID файла в GridFS
     * @return HTTP 204 No Content
     */
    ResponseEntity<Void> deleteImage(
            @PathVariable
            @NotBlank(message = "ID изображения обязателен")
            @Pattern(
                    regexp = "^[a-fA-F0-9]{24}$",
                    message = "ID изображения должен быть корректным ObjectId (24 шестнадцатеричных символа)"
            )
            String fileId);

    /**
     * 7️⃣ DELETE ALL — Удалить все изображения для конкретной сущности (SKU или Cargo).
     *
     * @param entityId ID сущности (SKU или Cargo)
     * @return HTTP 204 No Content
     */
    ResponseEntity<Void> deleteImagesByEntityId(
            @PathVariable
            @NotNull(message = "ID сущности обязателен")
            @Positive(message = "ID сущности должен быть положительным числом")
            Long entityId);

    /**
     * 8️⃣ GET PRIMARY — Получить главное изображение для сущности.
     *
     * @param entityId ID сущности (SKU или Cargo)
     * @return главное изображение
     */
    ResponseEntity<ImageViewResponse> getPrimaryImageByEntityId(
            @PathVariable
            @NotNull(message = "ID сущности обязателен")
            @Positive(message = "ID сущности должен быть положительным числом")
            Long entityId);

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

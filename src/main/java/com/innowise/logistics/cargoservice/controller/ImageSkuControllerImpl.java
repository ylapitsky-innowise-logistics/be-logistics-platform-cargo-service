package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.response.ImageSkuUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.innowise.logistics.cargoservice.mongo.service.ImageSkuServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated // Оставляем для работы каскадной валидации @Valid
@RestController
@RequestMapping("/api/v1/catalog/images/skus")
@RequiredArgsConstructor
public class ImageSkuControllerImpl implements ImageController<ImageSkuUploadRequest> {

    private final ImageSkuServiceImpl imageSkuService;


    // ===== 1️⃣ CREATE =====
    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") ImageSkuUploadRequest metadata) {

        log.debug("ImageSkuControllerImpl.uploadImage: Загрузка фото для SKU ID={}", metadata.getId());

        isEmptyFileCheck(file);
        isTooBigFileCheck(file);
        isItImageFileCheck(file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(imageSkuService.uploadImage(file, metadata));
    }


    // ===== 2️⃣ READ ALL =====
    @Override
    @GetMapping
    public ResponseEntity<PageResponse<ImageViewResponse>> getAllImages(
            @PageableDefault(size = 15, sort = "uploadedAt") Pageable pageable) {

        log.debug("ImageSkuControllerImpl.getAllImages: Получение всех SKU-изображений");
        return ResponseEntity.ok(imageSkuService.getAllImages(pageable));
    }


    // ===== 3️⃣ READ GALLERY =====
    @Override
    @GetMapping("/gallery/{entityId}")
    public ResponseEntity<PageResponse<ImageViewResponse>> getGalleryByEntityId(
            Long entityId,
            @PageableDefault(size = 10, sort = "sortOrder") Pageable pageable) {

        log.debug("ImageSkuControllerImpl.getGalleryByEntityId: Получение галереи для SKU ID={}", entityId);
        return ResponseEntity.ok(imageSkuService.getGalleryByEntityId(entityId, pageable));
    }


    // ===== 4️⃣ READ ONE =====
    @Override
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadImageByImageId(
            String fileId) {

        log.debug("ImageSkuControllerImpl.downloadImageByImageId: Скачивание файла ID={}", fileId);
        Resource resource = imageSkuService.downloadImageByImageId(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }


    // ===== 5️⃣ UPDATE =====
    @Override
    @PutMapping("/{fileId}")
    public ResponseEntity<ImageViewResponse> updateImageMetadata(
            String fileId,
            ImageSkuUploadRequest metadata) {

        log.debug("ImageSkuControllerImpl.updateImageMetadata: Обновление метаданных файла ID={}", fileId);
        return ResponseEntity.ok(imageSkuService.updateImageMetadata(fileId, metadata));
    }

    // ===== 6️⃣ DELETE ONE =====
    @Override
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteImage(
            String fileId) {

        log.debug("ImageSkuControllerImpl.deleteImage: Удаление файла ID={}", fileId);
        imageSkuService.deleteImage(fileId);
        return ResponseEntity.noContent().build();
    }


    // ===== 7️⃣ DELETE ALL =====
    @Override
    @DeleteMapping("/gallery/{entityId}")
    public ResponseEntity<Void> deleteImagesByEntityId(
            Long entityId) {

        log.debug("ImageSkuControllerImpl.deleteImagesByEntityId: Удаление всех фото для SKU ID={}", entityId);
        imageSkuService.deleteImagesByEntityId(entityId);
        return ResponseEntity.noContent().build();
    }

    // ===== 8️⃣ GET PRIMARY =====
    @Override
    @GetMapping("/gallery/{entityId}/primary")
    public ResponseEntity<ImageViewResponse> getPrimaryImageByEntityId(
            Long entityId) {

        log.debug("ImageSkuControllerImpl.getPrimaryImageByEntityId: Получение главного фото для SKU ID={}", entityId);
        return ResponseEntity.ok(imageSkuService.getPrimaryImageByEntityId(entityId));
    }
}

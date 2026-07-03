package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.ImageCargoUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.innowise.logistics.cargoservice.mongo.service.ImageCargoServiceImpl;
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

import static com.innowise.logistics.cargoservice.constant.ApiImageConstants.*;

@Slf4j
@RestController
@RequestMapping(IMAGE_CARGO_BASE_URL)
@RequiredArgsConstructor
@Validated
public class ImageCargoControllerImpl implements ImageController<ImageCargoUploadRequest> {

    private final ImageCargoServiceImpl imageCargoService;

    // ===== 1️⃣ CREATE =====
    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") ImageCargoUploadRequest metadata) {

        log.debug("ImageCargoControllerImpl.uploadImage: Загрузка фото для Cargo ID={}", metadata.getId());

        isEmptyFileCheck(file);
        isTooBigFileCheck(file);
        isItImageFileCheck(file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(imageCargoService.uploadImage(file, metadata));
    }

    // ===== 2️⃣ READ ALL =====
    @Override
    @GetMapping
    public ResponseEntity<PageResponse<ImageViewResponse>> getAllImages(
            @PageableDefault(size = 15, sort = "uploadedAt") Pageable pageable) {

        log.debug("ImageCargoControllerImpl.getAllImages: Получение всех Cargo-изображений");
        return ResponseEntity.ok(imageCargoService.getAllImages(pageable));
    }

    // ===== 3️⃣ READ GALLERY =====
    @Override
    @GetMapping(IMAGE_GALLERY_URL)
    public ResponseEntity<PageResponse<ImageViewResponse>> getGalleryByEntityId(
            Long entityId,
            @PageableDefault(size = 10, sort = "sortOrder") Pageable pageable) {

        log.debug("ImageCargoControllerImpl.getGalleryByEntityId: Получение галереи для Cargo ID={}", entityId);
        return ResponseEntity.ok(imageCargoService.getGalleryByEntityId(entityId, pageable));
    }

    // ===== 4️⃣ READ ONE =====
    @Override
    @GetMapping(IMAGE_DOWNLOAD_URL)
    public ResponseEntity<Resource> downloadImageByImageId(
            String fileId) {

        log.debug("ImageCargoControllerImpl.downloadImageByImageId: Скачивание файла ID={}", fileId);
        Resource resource = imageCargoService.downloadImageByImageId(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    // ===== 5️⃣ UPDATE =====
    @Override
    @PutMapping(IMAGE_DOWNLOAD_URL)
    public ResponseEntity<ImageViewResponse> updateImageMetadata(
            String fileId,
            ImageCargoUploadRequest metadata) {

        log.debug("ImageCargoControllerImpl.updateImageMetadata: Обновление метаданных файла ID={}", fileId);
        return ResponseEntity.ok(imageCargoService.updateImageMetadata(fileId, metadata));
    }

    // ===== 6️⃣ DELETE ONE =====
    @Override
    @DeleteMapping(IMAGE_DOWNLOAD_URL)
    public ResponseEntity<Void> deleteImage(
            String fileId) {

        log.debug("ImageCargoControllerImpl.deleteImage: Удаление файла ID={}", fileId);
        imageCargoService.deleteImage(fileId);
        return ResponseEntity.noContent().build();
    }

    // ===== 7️⃣ DELETE ALL =====
    @Override
    @DeleteMapping(IMAGE_GALLERY_URL)
    public ResponseEntity<Void> deleteImagesByEntityId(
            Long entityId) {

        log.debug("ImageCargoControllerImpl.deleteImagesByEntityId: Удаление всех фото для Cargo ID={}", entityId);
        imageCargoService.deleteImagesByEntityId(entityId);
        return ResponseEntity.noContent().build();
    }

    // ===== 8️⃣ GET PRIMARY =====
    @Override
    @GetMapping(IMAGE_PRIMARY_URL)
    public ResponseEntity<ImageViewResponse> getPrimaryImageByEntityId(
            Long entityId) {

        log.debug("ImageCargoControllerImpl.getPrimaryImageByEntityId: Получение главного фото для Cargo ID={}", entityId);
        return ResponseEntity.ok(imageCargoService.getPrimaryImageByEntityId(entityId));
    }
}

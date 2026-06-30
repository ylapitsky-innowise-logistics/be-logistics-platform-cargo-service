package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.ImageSkuUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.mongo.service.ImageSkuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/catalog/images/skus")
@RequiredArgsConstructor
@Validated
public class ImageSkuController {

    private final ImageSkuService imageSkuService;

    /**
     * 1️⃣ POST /api/v1/catalog/images/skus
     * Загрузка маркетинговой/каталожной фотографии для всей партии (артикула SKU)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadSkuImage(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") @Valid ImageSkuUploadRequest metadata) {
        log.debug("ImageSkuController.uploadSkuImage: Пришел запрос на 'POST /api/v1/catalog/images/skus', ImageSkuUploadRequest={}", metadata);
        return ResponseEntity.ok(imageSkuService.uploadSkuImage(file, metadata));
    }

    /**
     * 3️⃣ GET /api/v1/catalog/images/{fileId}
     * Стриминг бинарных чанков картинки из GridFS прямо в HTTP-ответ (просмотр/скачивание)
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadImage(
            @PathVariable String fileId) {
        log.debug("ImageSkuController.downloadImage: Пришел запрос на 'GET /api/v1/catalog/images/skus/{fileId}', fileId={}", fileId);
        GridFsResource resource = imageSkuService.downloadImage(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resource.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * 4️⃣ GET /api/v1/catalog/images/skus/{skuId}
     * Получение метаданных галереи для артикула
     */
    @GetMapping("/gallery/{skuId}")
    public ResponseEntity<List<ImageViewResponse>> getSkuGallery(
            @PathVariable Long skuId) {
        log.debug("ImageSkuController.getSkuGallery: Пришел запрос на 'GET /api/v1/catalog/images/skus/gallery/{skuId}, skuId={}'", skuId);        return ResponseEntity.ok(imageSkuService.getImagesBySkuId(skuId));
    }
}

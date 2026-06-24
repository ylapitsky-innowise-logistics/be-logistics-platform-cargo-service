package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.ImageCargoUploadRequest;
import com.innowise.logistics.cargoservice.dto.request.ImageSkuUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.mongo.service.ImageCargoService;
import com.innowise.logistics.cargoservice.mongo.service.ImageSkuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog/images")
@RequiredArgsConstructor
@Validated
public class ImageController {

    private final ImageSkuService imageSkuService;
    private final ImageCargoService imageCargoService;

    /**
     * 1️⃣ POST /api/v1/catalog/images/skus
     * Загрузка маркетинговой/каталожной фотографии для всей партии (артикула SKU)
     */
    @PostMapping(value = "/skus", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadSkuImage(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") @Valid ImageSkuUploadRequest metadata) {
        return ResponseEntity.ok(imageSkuService.uploadSkuImage(file, metadata));
    }

    /**
     * 2️⃣ POST /api/v1/catalog/images/cargos
     * Загрузка уникального фото конкретной единицы груза (например, фиксация брака/дефекта коробки)
     */
    @PostMapping(value = "/cargos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadCargoImage(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") @Valid ImageCargoUploadRequest metadata) {
        return ResponseEntity.ok(imageCargoService.uploadCargoImage(file, metadata));
    }

    /**
     * 3️⃣ GET /api/v1/catalog/images/{fileId}
     * Стриминг бинарных чанков картинки из GridFS прямо в HTTP-ответ (просмотр/скачивание)
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadImage(@PathVariable String fileId) {
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
    @GetMapping("/skus/{skuId}")
    public ResponseEntity<List<ImageViewResponse>> getSkuGallery(@PathVariable Long skuId) {
        return ResponseEntity.ok(imageSkuService.getImagesBySkuId(skuId));
    }

    /**
     * 5️⃣ GET /api/v1/catalog/images/cargos/{cargoId}
     * Получение уникальных метаданных картинок для конкретной коробки
     */
    @GetMapping("/cargos/{cargoId}")
    public ResponseEntity<List<ImageViewResponse>> getCargoGallery(@PathVariable Long cargoId) {
        return ResponseEntity.ok(imageCargoService.getImagesByCargoId(cargoId));
    }
}

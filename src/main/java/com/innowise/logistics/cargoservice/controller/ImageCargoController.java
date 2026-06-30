package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.ImageUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.mongo.service.ImageCargoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/catalog/images/cargos")
@RequiredArgsConstructor
@Validated
public class ImageCargoController {

    private final ImageCargoService imageCargoService;

    /**
     * 2️⃣ POST /api/v1/catalog/images/cargos
     * Загрузка уникального фото конкретной единицы груза (например, фиксация брака/дефекта коробки)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadCargoImage(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") @Valid ImageUploadRequest metadata) {
        log.debug("ImageCargoController.uploadCargoImage: Пришел запрос на 'POST /api/v1/catalog/images/cargos', ImageCargoUploadRequest={}", metadata);
        return ResponseEntity.ok(imageCargoService.uploadCargoImage(file, metadata));
    }

    /**
     * 5️⃣ GET /api/v1/catalog/images/cargos/{cargoId}
     * Получение уникальных метаданных картинок для конкретной коробки
     */
    @GetMapping("/{cargoId}")
    public ResponseEntity<List<ImageViewResponse>> getCargoGallery(
            @PathVariable Long cargoId) {
        log.debug("ImageCargoController.getCargoGallery: Пришел запрос на 'POST /api/v1/catalog/images/cargos/{cargoId}', ImageCargoUploadRequest={}", cargoId);
        return ResponseEntity.ok(imageCargoService.getImagesByCargoId(cargoId));
    }
}

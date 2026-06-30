package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.ImageCargoUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.mongo.service.ImageCargoService;
import com.innowise.logistics.cargoservice.mongo.service.ImageSkuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            @RequestPart("metadata") @Valid ImageCargoUploadRequest metadata) {
        return ResponseEntity.ok(imageCargoService.uploadCargoImage(file, metadata));
    }

    /**
     * 5️⃣ GET /api/v1/catalog/images/cargos/{cargoId}
     * Получение уникальных метаданных картинок для конкретной коробки
     */
    @GetMapping("/{cargoId}")
    public ResponseEntity<List<ImageViewResponse>> getCargoGallery(
            @PathVariable Long cargoId) {
        return ResponseEntity.ok(imageCargoService.getImagesByCargoId(cargoId));
    }
}

package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.mongo.service.ImageStorageService;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/catalog/images")
@RequiredArgsConstructor
public class ImageStorageController {

    private final ImageStorageService imageStorageService;
    private final GridFSBucket gridFSBucket;

    /**
     * 1️⃣ POST /api/v1/catalog/images
     * Загрузить картинку через форму (multipart/form-data)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        ImageUploadResponse response = imageStorageService.uploadImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2️⃣ GET /api/v1/catalog/images/{id}
     * Показать/стримить картинку прямо в браузере по ее ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getImageById(@PathVariable("id") String id) {
        // 1. Получаем файл из сервиса
        GridFSFile fileMetadata = imageStorageService.getImageFile(id);

        // 2. Безопасно вытаскиваем Content-Type (Провайдеры Mongo пишут его либо в метаданные, либо в корень)
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

//        if (fileMetadata.getMetadata() != null && fileMetadata.getMetadata().get("_contentType") != null) {
//            contentType = fileMetadata.getMetadata().get("_contentType").toString();
//        } else if (fileMetadata.getExtraElements() != null && fileMetadata.getExtraElements().get("contentType") != null) {
//            contentType = fileMetadata.getExtraElements().get("contentType").toString();
//        }

        // В современных драйверах Mongo метаданные гарантированно лежат в getMetadata()
        if (fileMetadata.getMetadata() != null && fileMetadata.getMetadata().get("_contentType") != null) {
            contentType = fileMetadata.getMetadata().get("_contentType").toString();
        } else if (fileMetadata.getMetadata() != null && fileMetadata.getMetadata().get("contentType") != null) {
            contentType = fileMetadata.getMetadata().get("contentType").toString();
        }

        // 3. Создаем GridFsResource напрямую через конструктор, передавая файл и открытый стрим скачивания
        GridFsResource resource = new GridFsResource(
                fileMetadata,
                gridFSBucket.openDownloadStream(fileMetadata.getObjectId())
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // inline заставляет браузер отобразить фото (а не скачивать его как файл)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}

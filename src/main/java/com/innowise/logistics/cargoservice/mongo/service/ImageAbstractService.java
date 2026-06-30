package com.innowise.logistics.cargoservice.mongo.service;

import com.innowise.logistics.cargoservice.dto.request.ImageUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.entity.Sku;
import com.innowise.logistics.cargoservice.mongo.entity.ImageSkuMetadata;
import com.mongodb.client.gridfs.GridFSBucket;
import jakarta.persistence.EntityNotFoundException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Setter(onMethod_ = @Autowired) // 🎯 Внедрение зависимостей через сеттеры, без конструкторов!
/**
 * T — Тип метаданных Mongo (ImageSkuMetadata или ImageCargoMetadata).
 * R — Тип репозитория метаданных Mongo (ImageSkuMetadataRepository или ImageCargoMetadataRepository).
 * P — Тип репозитория Postgres для проверки данных (SkuRepository или CargoRepository).
 */
public abstract class ImageAbstractService<
        T,
        R extends MongoRepository<T, String>,
        P extends JpaRepository<?, Long>
        > {

    protected GridFsTemplate gridFsTemplate;
    protected GridFSBucket gridFSBucket;

    // 🍃 Автоматически типизируемый репозиторий Mongo для метаданных
    protected R metadataRepository;

    // 🐘 Автоматически типизируемый репозиторий Postgres (SkuRepository или CargoRepository)
    protected P postgresRepository;


    protected ImageUploadResponse uploadImage(MultipartFile file, ImageUploadRequest request) {
//        if (file.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл изображения не может быть пустым");
//        }
//
//        // 1. Идем в Postgres за железно верными метаданными
//        Sku sku = postgresRepository.findById(request.getId())
//                .orElseThrow(() -> new EntityNotFoundException("Данные с ID " + request.getId() + " не найдены"));
//
//        try (InputStream inputStream = file.getInputStream()) {
//            // 2. Считываем ширину и высоту изображения программно
//            int width = 0;
//            int height = 0;
//            try {
//                BufferedImage bufferedImage = ImageIO.read(inputStream);
//                if (bufferedImage != null) {
//                    width = bufferedImage.getWidth();
//                    height = bufferedImage.getHeight();
//                }
//            } catch (Exception e) {
//                log.warn("Не удалось прочесть разрешение изображения {}", file.getOriginalFilename());
//            }
//
//            // 3. Сохраняем бинарник в GridFS
//            ObjectId fileId = gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType());
//
//            // 4. Заполняем паспорт метаданных
//            ImageSkuMetadata metadata = new ImageSkuMetadata();
//            metadata.setGridFsFileId(fileId.toHexString());
//            metadata.setSkuId(sku.getId());
//            metadata.setSkuName(sku.getName()); // Авто-защита от некорректного ввода фронтенда
//            metadata.setCategory(null); // Если у Sku появится категория в будущем, вызовем маппинг здесь
//
//            // Базовые поля
//            metadata.setFileName(file.getOriginalFilename());
//            metadata.setFileSize(file.getSize());
//            metadata.setMimeType(file.getContentType());
//            metadata.setWidth(width);
//            metadata.setHeight(height);
//            metadata.setDescription(request.getDescription());
//            metadata.setSortOrder(request.getSortOrder());
//            metadata.setIsPrimary(request.getIsPrimary());
//
//            imageSkuMetadataRepository.save(metadata);
//
//            String fileUrl = "/api/v1/catalog/images/" + fileId.toHexString();
//            return new ImageUploadResponse(fileId.toHexString(), fileUrl);
//
//        } catch (IOException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка сохранения картинки SKU");
//        }
        return null;
    }

//    public List<ImageViewResponse> getImagesBySkuId(Long skuId) {
//        return imageSkuMetadataRepository.findBySkuId(skuId).stream()
//                .map(meta -> new ImageViewResponse(
//                        meta.getGridFsFileId(),
//                        "/api/v1/catalog/images/" + meta.getGridFsFileId(),
//                        meta.getFileName(),
//                        meta.getMimeType(),
//                        meta.getFileSize(),
//                        meta.getDescription(),
//                        meta.getSortOrder(),
//                        meta.getIsPrimary()
//                )).toList();
//    }
}

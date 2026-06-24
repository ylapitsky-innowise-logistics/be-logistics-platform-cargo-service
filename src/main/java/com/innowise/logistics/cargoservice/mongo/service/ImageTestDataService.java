package com.innowise.logistics.cargoservice.mongo.service;

import com.innowise.logistics.cargoservice.dto.request.ImageSkuUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.entity.Sku;
import com.innowise.logistics.cargoservice.mongo.entity.ImageSkuMetadata;
import com.innowise.logistics.cargoservice.mongo.repository.ImageSkuMetadataRepository;
import com.innowise.logistics.cargoservice.repository.SkuRepository;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageTestDataService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFSBucket gridFSBucket;
    private final ImageSkuMetadataRepository imageSkuMetadataRepository;
    private final SkuRepository skuRepository; // 🎯 Подтягиваем для обогащения данных из Postgres

    public ImageUploadResponse uploadSkuImage(MultipartFile file, ImageSkuUploadRequest request) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл изображения не может быть пустым");
        }

        // 1. Идем в Postgres за железно верными метаданными артикула
        Sku sku = skuRepository.findById(request.getSkuId())
                .orElseThrow(() -> new EntityNotFoundException("Артикул (SKU) с ID " + request.getSkuId() + " не найден"));

        try (InputStream inputStream = file.getInputStream()) {
            // 2. Считываем ширину и высоту изображения программно
            int width = 0;
            int height = 0;
            try {
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                if (bufferedImage != null) {
                    width = bufferedImage.getWidth();
                    height = bufferedImage.getHeight();
                }
            } catch (Exception e) {
                log.warn("Не удалось прочесть разрешение изображения {}", file.getOriginalFilename());
            }

            // 3. Сохраняем бинарник в GridFS
            ObjectId fileId = gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType());

            // 4. Заполняем паспорт метаданных
            ImageSkuMetadata metadata = new ImageSkuMetadata();
            metadata.setGridFsFileId(fileId.toHexString());
            metadata.setSkuId(sku.getId());
            metadata.setSkuName(sku.getName()); // Авто-защита от некорректного ввода фронтенда
            metadata.setCategory(null); // Если у Sku появится категория в будущем, вызовем маппинг здесь

            // Базовые поля
            metadata.setFileName(file.getOriginalFilename());
            metadata.setFileSize(file.getSize());
            metadata.setMimeType(file.getContentType());
            metadata.setWidth(width);
            metadata.setHeight(height);
            metadata.setDescription(request.getDescription());
            metadata.setSortOrder(request.getSortOrder());
            metadata.setIsPrimary(request.getIsPrimary());

            imageSkuMetadataRepository.save(metadata);

            String fileUrl = "/api/v1/catalog/images/" + fileId.toHexString();
            return new ImageUploadResponse(fileId.toHexString(), fileUrl);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка сохранения картинки SKU");
        }
    }

    public List<ImageViewResponse> getImagesBySkuId(Long skuId) {
        return imageSkuMetadataRepository.findBySkuId(skuId).stream()
                .map(meta -> new ImageViewResponse(
                        meta.getGridFsFileId(),
                        "/api/v1/catalog/images/" + meta.getGridFsFileId(),
                        meta.getFileName(),
                        meta.getMimeType(),
                        meta.getFileSize(),
                        meta.getDescription(),
                        meta.getSortOrder(),
                        meta.getIsPrimary()
                )).toList();
    }

    // Универсальный метод скачивания из GridFS (используется общим контроллером)
    public GridFsResource downloadImage(String fileId) {
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
        if (gridFSFile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Изображение не найдено");
        }
        return new GridFsResource(gridFSFile, gridFSBucket.openDownloadStream(gridFSFile.getObjectId()));
    }
}

package com.innowise.logistics.cargoservice.mongo.service;

import com.innowise.logistics.cargoservice.dto.request.ImageMetadataUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.mongo.entity.ImageSkuMetadata;
import com.innowise.logistics.cargoservice.mongo.repository.ImageSkuMetadataRepository;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
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

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageSkuService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFSBucket gridFSBucket;
    private final ImageSkuMetadataRepository imageSkuMetadataRepository;

    /**
     * 1️⃣ Загрузка изображения в MongoDB GridFS + Регистрация в image_metadata
     */
    public ImageUploadResponse uploadImage(MultipartFile file, ImageMetadataUploadRequest metadataRequest) {
        log.info("Попытка загрузки файла в MongoDB: {}, размер: {} байт, атрибуты: {}",
                file.getOriginalFilename(), file.getSize(), metadataRequest);

        // Валидация на пустой файл
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не может быть пустым");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // 1. Сохраняем физический файл в GridFS. Он вернет уникальный ObjectId
            ObjectId fileId = gridFsTemplate.store(
                    inputStream,
                    file.getOriginalFilename(),
                    file.getContentType()
            );

            log.info("Физический файл успешно сохранен в GridFS. Назначен ID: {}", fileId);

            // 2. Создаем и сохраняем бизнес-паспорт файла
            ImageSkuMetadata metadata = new ImageSkuMetadata();
            metadata.setGridFsFileId(fileId.toHexString());
            metadata.setSkuId(metadataRequest.getSkuId());
            metadata.setSkuName(metadataRequest.getSkuName());
            metadata.setCargoName(metadataRequest.getCargoName());
            metadata.setCargoCategory(metadataRequest.getCargoCategory());
            metadata.setDescription(metadataRequest.getDescription());
            // Поле uploadedAt заполнится автоматически благодаря @CreatedDate в сущности

            ImageSkuMetadata savedMetadata = imageSkuMetadataRepository.save(metadata);
            log.info("Бизнес-метаданные фотографии успешно сохранены с Mongo-ID документа: {}", savedMetadata.getId());

            // 3. Формируем эндпоинт для скачивания (отдаем ID из GridFS, так как контроллер ищет по нему)
            String fileUrl = "/api/v1/catalog/images/" + fileId.toHexString();

            return new ImageUploadResponse(fileId.toHexString(), fileUrl);

        } catch (IOException e) {
            log.error("Критическая ошибка ввода-вывода при сохранении файла", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось сохранить изображение");
        }    }

    /**
     * 2️⃣ Скачивание / Просмотр изображения по его ID
     */
    public GridFsResource downloadImage(String fileId) {
        log.debug("Запрос на получение файла из MongoDB по ID: {}", fileId);

        // Ищем метаданные файла в коллекции fs.files
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));

        if (gridFSFile == null) {
            log.warn("Файл с ID = {} не найден в MongoDB", fileId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Изображение не найдено в хранилище");
        }

        // Открываем потоковое чтение бинарных чанков из fs.chunks через GridFSBucket
        return new GridFsResource(gridFSFile, gridFSBucket.openDownloadStream(gridFSFile.getObjectId()));
    }

    /**
     * 2️⃣ Нахождение метаданных изображения по его ID
     */
    public GridFSFile getImageFile(String fileId) {
        log.debug("Запрос на получение файла из MongoDB по ID: {}", fileId);

        // Ищем метаданные файла в системной коллекции fs.files
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));

        if (gridFSFile == null) {
            log.warn("Файл с ID = {} не найден в MongoDB", fileId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Изображение не найдено в хранилище");
        }

        return gridFSFile;
    }
}

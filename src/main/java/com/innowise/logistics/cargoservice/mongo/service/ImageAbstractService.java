package com.innowise.logistics.cargoservice.mongo.service;

import com.innowise.logistics.cargoservice.dto.request.ImageUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

// Сюда переедет общая логика для downloadImage, getAllImages и deleteImage!
/**
 * Базовый абстрактный сервис со сквозной параметризацией для управления медиа-контентом.
 *
 * @param <T>       Тип сущности метаданных Mongo (ImageSkuMetadata или ImageCargoMetadata)
 * @param <R>       Тип репозитория метаданных Mongo (ImageSkuMetadataRepository или ImageCargoMetadataRepository)
 * @param <E>       Тип сущности Postgres (Sku или Cargo)
 * @param <P>       Тип репозитория Postgres (SkuRepository или CargoRepository)
 * @param <REQUEST> Тип DTO запроса (ImageSkuUploadRequest или ImageCargoUploadRequest)
 */
@Slf4j
@Setter(onMethod_ = @Autowired) // 🎯 Setter Injection — чистый DI для абстрактных классов
public abstract class ImageAbstractService<
        T,
        R extends MongoRepository<T, String>,
        E,
        P extends JpaRepository<E, Long>,
        REQUEST
        > implements ImageService<REQUEST> {

    protected GridFsTemplate gridFsTemplate;
    protected GridFSBucket gridFSBucket;

    // 🍃 Автоматически типизируемый репозиторий Mongo для метаданных
    protected R metadataRepository;

    // 🐘 Автоматически типизируемый репозиторий Postgres (SkuRepository или CargoRepository)
    protected P postgresRepository;


    /**
     * 1️⃣ CREATE — Специфичен для каждого сервиса, так как маппинг полей Sku и Cargo отличается.
     */
    @Override
    public abstract ImageUploadResponse uploadImage(MultipartFile file, REQUEST metadata);

    /**
     * 3️⃣ READ GALLERY — Специфичен для каждого сервиса, так как методы репозиториев Mongo
     * называются по-разному (findBySkuId и findByCargoId).
     */
    @Override
    public abstract PageResponse<ImageViewResponse> getGalleryByEntityId(Long entityId, Pageable pageable);

    /**
     * 4️⃣ READ ONE — Универсальный стриминг бинарных файлов из GridFS для SKU и Cargo на базе общего fileId.
     */
    @Override
    public Resource downloadImageByImageId(String fileId) {
        log.debug("ImageAbstractService: Извлечение бинарного файла из GridFS, fileId={}", fileId);

        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
        if (gridFSFile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Изображение с ID " + fileId + " не найдено в GridFS");
        }

        return new GridFsResource(gridFSFile, gridFSBucket.openDownloadStream(gridFSFile.getObjectId()));
    }

    /**
     * 6️⃣ DELETE ONE — Каноничное удаление из GridFS и синхронное удаление паспорта метаданных из Mongo.
     */
    @Override
    public void deleteImage(String fileId) {
        log.debug("ImageAbstractService: Каскадное удаление изображения, fileId={}", fileId);

        if (!metadataRepository.existsById(fileId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Метаданные изображения с ID " + fileId + " не найдены");
        }

        // Сначала зачищаем метаданные
        metadataRepository.deleteById(fileId);
        // Затем удаляем сам бинарный файл из чанков GridFS
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId)));
    }

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

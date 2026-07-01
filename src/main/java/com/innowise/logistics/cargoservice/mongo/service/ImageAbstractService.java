package com.innowise.logistics.cargoservice.mongo.service;

import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.innowise.logistics.cargoservice.mongo.entity.BaseImageMetadata;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;


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
public abstract class ImageAbstractService<
        T extends BaseImageMetadata,
        R extends MongoRepository<T, String>,
        E,
        P extends JpaRepository<E, Long>,
        REQUEST
        > implements ImageService<REQUEST> {

    protected final GridFsTemplate gridFsTemplate;
    protected final GridFSBucket gridFSBucket;

    // 🍃 Автоматически типизируемый репозиторий Mongo для метаданных
    protected final R metadataRepository;

    // 🐘 Автоматически типизируемый репозиторий Postgres (SkuRepository или CargoRepository)
    protected final P postgresRepository;


    protected ImageAbstractService(GridFsTemplate gridFsTemplate,
                                   GridFSBucket gridFSBucket,
                                   R metadataRepository,
                                   P postgresRepository) {
        this.gridFsTemplate = gridFsTemplate;
        this.gridFSBucket = gridFSBucket;
        this.metadataRepository = metadataRepository;
        this.postgresRepository = postgresRepository;
    }



    // ===== ОБЩИЕ МЕТОДЫ =====

    /**
     * Сохраняет файл в GridFS и возвращает ObjectId.
     */
    protected String storeFileInGridFS(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            ObjectId fileId = gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType());
            return fileId.toHexString();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка сохранения файла в GridFS");
        }
    }

    /**
     * Извлекает размеры изображения.
     */
    protected ImageDimension extractImageDimension(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            if (bufferedImage != null) {
                return new ImageDimension(bufferedImage.getWidth(), bufferedImage.getHeight());
            }
        } catch (IOException e) {
            log.warn("Не удалось прочитать размеры изображения: {}", file.getOriginalFilename());
        }
        return new ImageDimension(0, 0);
    }

    /**
     * Заполняет базовые поля метаданных из файла и запроса.
     */
    protected T fillBasicMetadata(T metadata, MultipartFile file, ImageDimension dimension, REQUEST request) {
        metadata.setFileName(file.getOriginalFilename());
        metadata.setFileSize(file.getSize());
        metadata.setMimeType(file.getContentType());
        metadata.setWidth(dimension.width());
        metadata.setHeight(dimension.height());
        metadata.setDescription(getDescription(request));
        metadata.setSortOrder(getSortOrder(request));
        metadata.setIsPrimary(getIsPrimary(request));
        return metadata;
    }



// ===== АБСТРАКТНЫЕ МЕТОДЫ ДЛЯ НАСЛЕДНИКОВ =====

    protected abstract Long getEntityId(REQUEST request);

    protected abstract E findEntityById(Long id);

    protected abstract T createMetadata(E entity, String fileId, REQUEST request, ImageDimension dimension, MultipartFile file);

    protected abstract String getDescription(REQUEST request);

    protected abstract Integer getSortOrder(REQUEST request);

    protected abstract Boolean getIsPrimary(REQUEST request);

    protected abstract Page<T> findMetadataByEntityId(Long entityId, Pageable pageable);



    // ===== РЕАЛИЗАЦИЯ МЕТОДОВ ИНТЕРФЕЙСА =====

    /**
     * 1️⃣ CREATE — Специфичен для каждого сервиса, так как маппинг полей Sku и Cargo отличается.
     */
    @Override
    public ImageUploadResponse uploadImage(MultipartFile file, REQUEST request) {
        log.debug("Загрузка изображения для entity ID={}", getEntityId(request));

        // 1. Валидация файла
//        validateFile(file);

        // 2. Проверяем существование сущности в Postgres
        Long entityId = getEntityId(request);
        E entity = findEntityById(entityId);

        // 3. Извлекаем размеры изображения
        ImageDimension dimension = extractImageDimension(file);

        // 4. Сохраняем файл в GridFS
        String fileId = storeFileInGridFS(file);

        // 5. Создаём и сохраняем метаданные
        T metadata = createMetadata(entity, fileId, request, dimension, file);
        T savedMetadata = metadataRepository.save(metadata);

        // 6. Формируем ответ
        String fileUrl = buildFileUrl(fileId);
        return new ImageUploadResponse(fileId, fileUrl);
    }

    @Override
    public PageResponse<ImageViewResponse> getAllImages(Pageable pageable) {
        log.debug("Получение всех изображений с пагинацией");
        Page<T> page = metadataRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toImageViewResponse));
    }


    /**
     * 3️⃣ READ GALLERY — Специфичен для каждого сервиса, так как методы репозиториев Mongo
     * называются по-разному (findBySkuId и findByCargoId).
     */
    @Override
    public PageResponse<ImageViewResponse> getGalleryByEntityId(Long entityId, Pageable pageable) {
        log.debug("Получение галереи для entity ID={}", entityId);
        Page<T> page = findMetadataByEntityId(entityId, pageable);
        return PageResponse.from(page.map(this::toImageViewResponse));
    }

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

    @Override
    public ImageViewResponse updateImageMetadata(String fileId, REQUEST request) {
        log.debug("Обновление метаданных, fileId={}", fileId);

        T metadata = metadataRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Метаданные не найдены"));

        metadata.setDescription(getDescription(request));
        metadata.setSortOrder(getSortOrder(request));
        metadata.setIsPrimary(getIsPrimary(request));

        T updated = metadataRepository.save(metadata);
        return toImageViewResponse(updated);
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

    @Override
    public void deleteImagesByEntityId(Long entityId) {
        log.debug("Удаление всех изображений для entity ID={}", entityId);
        // Реализуется в наследниках, так как методы поиска по ID разные
        deleteAllByEntityId(entityId);
    }

    @Override
    public ImageViewResponse getPrimaryImageByEntityId(Long entityId) {
        log.debug("Получение главного изображения для entity ID={}", entityId);
        // Реализуется в наследниках
        return findPrimaryByEntityId(entityId);
    }



    // ===== ЗАЩИЩЁННЫЕ АБСТРАКТНЫЕ МЕТОДЫ ДЛЯ НАСЛЕДНИКОВ =====

    protected abstract void deleteAllByEntityId(Long entityId);

    protected abstract ImageViewResponse findPrimaryByEntityId(Long entityId);




    // ===== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =====

    // у меня валидация на уровне контроллера (дефолтные методы интерфейса)!
//    protected void validateFile(MultipartFile file) {
//        if (file == null || file.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не может быть пустым");
//        }
//        if (file.getSize() > 10 * 1024 * 1024) {
//            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Файл не должен превышать 10 MB");
//        }
//        String contentType = file.getContentType();
//        if (contentType == null || !contentType.startsWith("image/")) {
//            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Допустимы только изображения");
//        }
//    }

    protected String buildFileUrl(String fileId) {
        return "/api/v1/catalog/images/" + fileId;
    }

    protected ImageViewResponse toImageViewResponse(T metadata) {
        return new ImageViewResponse(
                metadata.getGridFsFileId(),
                buildFileUrl(metadata.getGridFsFileId()),
                metadata.getFileName(),
                metadata.getMimeType(),
                metadata.getFileSize(),
                metadata.getDescription(),
                metadata.getSortOrder(),
                metadata.getIsPrimary()
        );
    }



    // ===== ВНУТРЕННИЙ КЛАСС =====

    protected record ImageDimension(int width, int height) {}
}

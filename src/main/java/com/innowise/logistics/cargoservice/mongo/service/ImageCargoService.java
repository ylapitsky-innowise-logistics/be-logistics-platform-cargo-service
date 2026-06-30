package com.innowise.logistics.cargoservice.mongo.service;

import com.innowise.logistics.cargoservice.dto.request.ImageUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageUploadResponse;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Sku;
import com.innowise.logistics.cargoservice.mongo.entity.ImageCargoMetadata;
import com.innowise.logistics.cargoservice.mongo.repository.ImageCargoMetadataRepository;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import com.innowise.logistics.cargoservice.repository.SkuRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class ImageCargoService {

    private final GridFsTemplate gridFsTemplate;
    private final ImageCargoMetadataRepository imageCargoMetadataRepository;
    private final CargoRepository cargoRepository; // 🎯 Подтягиваем для денормализации данных Cargo
    private final SkuRepository skuRepository;

    @Transactional
    public ImageUploadResponse uploadCargoImage(MultipartFile file, ImageUploadRequest request) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не может быть пустым");
        }

        Cargo cargo = cargoRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Груз (Cargo) с ID " + request.getId() + " не найден"));

        // 2. Вытаскиваем Sku отдельно (чтобы избежать LazyInitializationException)
        Sku sku = cargo.getSku();

        try (InputStream inputStream = file.getInputStream()) {
            int width = 0, height = 0;
            try {
                BufferedImage bi = ImageIO.read(file.getInputStream());
                if (bi != null) { width = bi.getWidth(); height = bi.getHeight(); }
            } catch (Exception ignored) {}

            ObjectId fileId = gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType());

            ImageCargoMetadata metadata = new ImageCargoMetadata();
            metadata.setGridFsFileId(fileId.toHexString());
            metadata.setCargoId(cargo.getId());
            metadata.setCargoName(cargo.getName());
            metadata.setSkuName(sku.getName());
            metadata.setCargoCategory(cargo.getCategory());

            metadata.setFileName(file.getOriginalFilename());
            metadata.setFileSize(file.getSize());
            metadata.setMimeType(file.getContentType());
            metadata.setWidth(width);
            metadata.setHeight(height);
            metadata.setDescription(request.getDescription());
            metadata.setSortOrder(request.getSortOrder());
            metadata.setIsPrimary(request.getIsPrimary());

            imageCargoMetadataRepository.save(metadata);

//            testImageRunner.generateTestImage();
//            new TestImageRunner().generateTestImage();


            return new ImageUploadResponse(fileId.toHexString(), "/api/v1/catalog/images/" + fileId.toHexString());

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка сохранения уникального фото груза");
        }
    }

    public List<ImageViewResponse> getImagesByCargoId(Long cargoId) {
        return imageCargoMetadataRepository.findByCargoId(cargoId).stream()
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
}

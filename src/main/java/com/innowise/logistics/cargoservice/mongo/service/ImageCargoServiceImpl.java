package com.innowise.logistics.cargoservice.mongo.service;

import com.innowise.logistics.cargoservice.dto.request.ImageCargoUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.mongo.entity.ImageCargoMetadata;
import com.innowise.logistics.cargoservice.mongo.repository.ImageCargoMetadataRepository;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import com.mongodb.client.gridfs.GridFSBucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static com.innowise.logistics.cargoservice.constant.ApiImageConstants.IMAGE_CARGO_BASE_URL;

@Slf4j
@Service
public class ImageCargoServiceImpl extends ImageAbstractService<
                                                ImageCargoMetadata,
                                                ImageCargoMetadataRepository,
                                                Cargo,
                                                CargoRepository,
                                                ImageCargoUploadRequest> {

    public ImageCargoServiceImpl(GridFsTemplate gridFsTemplate,
                                 GridFSBucket gridFSBucket,
                                 ImageCargoMetadataRepository metadataRepository,
                                 CargoRepository cargoRepository) {
        super(gridFsTemplate, gridFSBucket, metadataRepository, cargoRepository);
    }

    @Override
    protected Long getEntityId(ImageCargoUploadRequest request) {
        return request.getId();
    }

    @Override
    protected Cargo findEntityById(Long id) {
        return postgresRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Груз (Cargo) с ID " + id + " не найден"
                ));
    }

    @Override
    protected ImageCargoMetadata createMetadata(Cargo entity,
                                                String fileId,
                                                ImageCargoUploadRequest request,
                                                ImageDimension dimension,
                                                MultipartFile file) {
        ImageCargoMetadata metadata = new ImageCargoMetadata();
        metadata.setGridFsFileId(fileId);
        metadata.setCargoId(entity.getId());
        metadata.setCargoName(entity.getName());
        metadata.setSkuName(entity.getSku().getName());
        metadata.setCargoCategory(entity.getCategory());

        fillBasicMetadata(metadata, file, dimension, request);
        return metadata;
    }

    @Override
    protected String getDescription(ImageCargoUploadRequest request) {
        return request.getDescription();
    }

    @Override
    protected Integer getSortOrder(ImageCargoUploadRequest request) {
        return request.getSortOrder() != null ? request.getSortOrder() : 0;
    }

    @Override
    protected Boolean getIsPrimary(ImageCargoUploadRequest request) {
        return request.getIsPrimary() != null && request.getIsPrimary();
    }

    @Override
    protected Page<ImageCargoMetadata> findMetadataByEntityId(Long entityId, Pageable pageable) {
        return metadataRepository.findByCargoId(entityId, pageable);
    }

    @Override
    protected void deleteAllByEntityId(Long entityId) {
        int pageSize = 100;
        int pageNumber = 0;
        Page<ImageCargoMetadata> page;

        do {
            page = metadataRepository.findByCargoId(entityId, PageRequest.of(pageNumber, pageSize));
            for (ImageCargoMetadata metadata : page.getContent()) {
                gridFsTemplate.delete(new Query(Criteria.where("_id").is(metadata.getGridFsFileId())));
            }
            metadataRepository.deleteAll(page.getContent());
            pageNumber++;
        } while (page.hasNext());
    }

    @Override
    protected ImageViewResponse findPrimaryByEntityId(Long entityId) {
        return metadataRepository.findByCargoIdAndIsPrimaryTrue(entityId)
                .map(this::toImageViewResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Главное изображение для Cargo ID " + entityId + " не найдено"
                ));
    }

    @Override
    protected String buildFileUrl(String fileId) {
        return IMAGE_CARGO_BASE_URL + '/' + fileId;
    }
}

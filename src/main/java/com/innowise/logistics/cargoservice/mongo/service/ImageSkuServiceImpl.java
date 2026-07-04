package com.innowise.logistics.cargoservice.mongo.service;

import com.innowise.logistics.cargoservice.dto.request.ImageSkuUploadRequest;
import com.innowise.logistics.cargoservice.dto.response.ImageViewResponse;
import com.innowise.logistics.cargoservice.entity.Sku;
import com.innowise.logistics.cargoservice.mongo.entity.ImageSkuMetadata;
import com.innowise.logistics.cargoservice.mongo.repository.ImageSkuMetadataRepository;
import com.innowise.logistics.cargoservice.repository.SkuRepository;
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

import static com.innowise.logistics.cargoservice.constant.ApiImageConstants.IMAGE_SKU_BASE_URL;

@Slf4j
@Service
public class ImageSkuServiceImpl extends ImageAbstractService<
                                                ImageSkuMetadata,
                                                ImageSkuMetadataRepository,
                                                Sku,
                                                SkuRepository,
                                                ImageSkuUploadRequest> {

    public ImageSkuServiceImpl(GridFsTemplate gridFsTemplate,
                               GridFSBucket gridFSBucket,
                               ImageSkuMetadataRepository metadataRepository,
                               SkuRepository skuRepository) {
        super(gridFsTemplate, gridFSBucket, metadataRepository, skuRepository);
    }


    @Override
    protected Long getEntityId(ImageSkuUploadRequest request) {
        return request.getId();
    }

    @Override
    protected Sku findEntityById(Long id) {
        return postgresRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Артикул (SKU) с ID " + id + " не найден"
                ));
    }

    @Override
    protected ImageSkuMetadata createMetadata(Sku entity,
                                            String fileId,
                                            ImageSkuUploadRequest request,
                                            ImageDimension dimension,
                                            MultipartFile file) {
        ImageSkuMetadata metadata = new ImageSkuMetadata();
        metadata.setGridFsFileId(fileId);
        metadata.setSkuId(entity.getId());
        metadata.setSkuName(entity.getName());
        metadata.setCategory(null); // TODO: добавить категорию, если появится в Sku

        fillBasicMetadata(metadata, file, dimension, request);
        return metadata;
    }

    @Override
    protected String getDescription(ImageSkuUploadRequest request) {
        return request.getDescription();
    }

    @Override
    protected Integer getSortOrder(ImageSkuUploadRequest request) {
        return request.getSortOrder() != null ? request.getSortOrder() : 0;
    }

    @Override
    protected Boolean getIsPrimary(ImageSkuUploadRequest request) {
        return request.getIsPrimary() != null && request.getIsPrimary();
    }

    @Override
    protected Page<ImageSkuMetadata> findMetadataByEntityId(Long entityId, Pageable pageable) {
        return metadataRepository.findBySkuId(entityId, pageable);
    }


    @Override
    protected void deleteAllByEntityId(Long entityId) {
        int pageSize = 100;
        int pageNumber = 0;
        Page<ImageSkuMetadata> page;

        do {
            page = metadataRepository.findBySkuId(entityId, PageRequest.of(pageNumber, pageSize));
            for (ImageSkuMetadata metadata : page.getContent()) {
                gridFsTemplate.delete(new Query(Criteria.where("_id").is(metadata.getGridFsFileId())));
            }
            metadataRepository.deleteAll(page.getContent());
            pageNumber++;
        } while (page.hasNext());
    }

    @Override
    protected ImageViewResponse findPrimaryByEntityId(Long entityId) {
        return metadataRepository.findBySkuIdAndIsPrimaryTrue(entityId)
                .map(this::toImageViewResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Главное изображение для SKU ID " + entityId + " не найдено"
                ));
    }

    @Override
    protected String buildFileUrl(String fileId) {
        return IMAGE_SKU_BASE_URL + '/' + fileId;
    }
}

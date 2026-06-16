package com.innowise.logistics.cargoservice.mongo.repository;

import com.innowise.logistics.cargoservice.mongo.entity.ImageMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageMetadataRepository extends MongoRepository<ImageMetadata, String> {

    java.util.List<ImageMetadata> findByCargoId(Long cargoId);
    java.util.List<ImageMetadata> findByIsDamagedReportTrue();
}
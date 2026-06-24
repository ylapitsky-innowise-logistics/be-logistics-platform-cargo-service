package com.innowise.logistics.cargoservice.mongo.repository;

import com.innowise.logistics.cargoservice.mongo.entity.ImageSkuMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageSkuMetadataRepository extends MongoRepository<ImageSkuMetadata, String> {
    List<ImageSkuMetadata> findBySkuId(Long skuId); // 🟢 Находим галерею артикула
}
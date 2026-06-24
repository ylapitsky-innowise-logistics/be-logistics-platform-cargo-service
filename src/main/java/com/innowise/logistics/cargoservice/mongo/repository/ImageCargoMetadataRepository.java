package com.innowise.logistics.cargoservice.mongo.repository;

import com.innowise.logistics.cargoservice.mongo.entity.ImageCargoMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImageCargoMetadataRepository extends MongoRepository<ImageCargoMetadata, String> {
    List<ImageCargoMetadata> findByCargoId(Long cargoId); // 🟢 Находим уникальные фото коробки
}

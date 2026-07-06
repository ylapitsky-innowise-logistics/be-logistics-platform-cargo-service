package com.innowise.logistics.cargoservice.mongo.repository;

import com.innowise.logistics.cargoservice.mongo.entity.ImageCargoMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageCargoMetadataRepository extends MongoRepository<ImageCargoMetadata, String> {

    // 📖 Получить все изображения для Cargo (с пагинацией)
    Page<ImageCargoMetadata> findByCargoId(Long cargoId, Pageable pageable);

    // 📖 Найти главное изображение для Cargo
//    Optional<ImageCargoMetadata> findByCargoIdAndIsPrimaryTrue(Long cargoId);
    Optional<ImageCargoMetadata> findFirstByCargoIdAndIsPrimaryTrue(Long cargoId);

    // 🗑️ Удалить все изображения для Cargo
    void deleteByCargoId(Long cargoId);

    // 🔍 Проверить, есть ли изображения для Cargo
    boolean existsByCargoId(Long cargoId);
}

package com.innowise.logistics.cargoservice.mongo.repository;

import com.innowise.logistics.cargoservice.mongo.entity.ImageSkuMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageSkuMetadataRepository extends MongoRepository<ImageSkuMetadata, String> {

    // 📖 Получить все изображения для SKU (с пагинацией)
    Page<ImageSkuMetadata> findBySkuId(Long skuId, Pageable pageable);

    // 📖 Найти главное изображение для SKU
//    Optional<ImageSkuMetadata> findBySkuIdAndIsPrimaryTrue(Long skuId);
    Optional<ImageSkuMetadata> findFirstBySkuIdAndIsPrimaryTrue(Long skuId);

    // 🗑️ Удалить все изображения для SKU
    void deleteBySkuId(Long skuId);

    // 🔍 Проверить, есть ли изображения для SKU
    boolean existsBySkuId(Long skuId);
}

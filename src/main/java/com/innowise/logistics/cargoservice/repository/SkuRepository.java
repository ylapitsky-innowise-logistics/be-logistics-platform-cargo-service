package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Sku;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkuRepository extends JpaRepository<Sku, Long> {

    /**
     * Найти все SKU с фильтрацией по активности.
     *
     * @param isActive фильтр активности:
     *                 - true: только активные SKU (isActive = true)
     *                 - false: только НЕ активные SKU (isActive = false)
     *                 - null: все SKU (без фильтрации)
     * @param pageable параметры пагинации и сортировки
     * @return страница с SKU
     */
    @Query("SELECT s FROM Sku s WHERE (:isActive IS NULL OR s.isActive = :isActive)")
    Page<Sku> findAllByActive(@Param("isActive") Boolean isActive, Pageable pageable);

    // Ищем SKU по уникальному имени (бизнес-ключу)
    Optional<Sku> findByName(String name);


//    /**
//     * Найти все активные SKU (isActive = true) с пагинацией и сортировкой.
//     * Pageable автоматически обрабатывает сортировку по любому полю.
//     *
//     * @param pageable параметры пагинации и сортировки
//     * @return страница с активными SKU
//     */
//    Page<Sku> findAllByIsActiveTrue(Pageable pageable);
//
//    /**
//     * Найти все SKU с пагинацией и сортировкой.
//     * Pageable автоматически обрабатывает сортировку по любому полю.
//     *
//     * @param pageable параметры пагинации и сортировки
//     * @return страница с SKU
//     */
//    Page<Sku> findAll(Pageable pageable);
//
//    /**
//     * Найти все активные SKU с пагинацией и сортировкой.
//     *
//     * @param pageable параметры пагинации и сортировки
//     * @return страница с активными SKU
//     */
//    Page<Sku> findAllByIsActiveTrue(Pageable pageable);
//
//    /**
//     * Найти SKU по имени (с пагинацией и сортировкой).
//     *
//     * @param name     имя артикула (частичное совпадение)
//     * @param pageable параметры пагинации и сортировки
//     * @return страница с SKU
//     */
//    @Query("SELECT s FROM Sku s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
//    Page<Sku> findByNameContainingIgnoreCase(String name, Pageable pageable);
//
//    /**
//     * Найти активные SKU по имени (с пагинацией и сортировкой).
//     *
//     * @param name     имя артикула (частичное совпадение)
//     * @param pageable параметры пагинации и сортировки
//     * @return страница с активными SKU
//     */
//    @Query("SELECT s FROM Sku s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) AND s.isActive = true")
//    Page<Sku> findActiveByNameContainingIgnoreCase(String name, Pageable pageable);
}

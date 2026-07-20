package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.dto.SkuStats;
import com.innowise.logistics.cargoservice.dto.response.SkuAvailabilityResponse;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Status;
import com.innowise.logistics.cargoservice.repository.projection.CargoReservationProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Long> {

    /**
     * Получить Cargo по ID и сразу подтянуть связанный Sku (решает LazyInitializationException)
     */
    @Query("SELECT c FROM Cargo c LEFT JOIN FETCH c.sku WHERE c.id = :id")
    Optional<Cargo> findByIdWithSku(@Param("id") Long id);

    /**
     * Найти первые N доступных товаров по 'артикулу' и 'статусу'
     *
     * @param skuId  id артикула
     * @param status статус товара
     * @param limit  максимальное количество записей (N)
     * @return список товаров (не более limit)
     */
    @Query("""
             SELECT c 
            FROM Cargo c 
            WHERE c.sku.id = :skuId 
              AND c.status = :status 
            ORDER BY c.id
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE) // чтобы 2 параллельных запроса не могли забронировать 1 и тот же товар - пессимистич. блокировку
    List<Cargo> findFirstNAvailableBySkuIdAndStatus(@Param("skuId") Long skuId,
                                                    @Param("status") Status status,
                                                    Pageable limit);

    /**
     * Подсчитать количество доступных товаров по 'артикулу' и 'статусу'
     *
     * @param skuId  id артикула
     * @param status статус товара
     * @return количество доступных единиц
     */
    long countBySkuIdAndStatus(Long skuId, Status status);


    /**
     * Пагинированный отчет по доступности SKU на складах.
     * Статус передается параметром во избежание ошибок нативного приведения типов в СУБД.
     */
    @Query(value = """
            SELECT new com.innowise.logistics.cargoservice.dto.response.SkuAvailabilityResponse(
                c.sku,
                MIN(c.name),
                MIN(c.category),
                MIN(c.weight),
                c.dimension,
                MIN(c.price),
                MAX(c.price),
                MIN(c.createdAt),
                MAX(c.createdAt),
                COUNT(c)
            )
            FROM Cargo c
            WHERE c.status = :status
            GROUP BY c.sku, c.sku.id, c.dimension
            """,
            countQuery = """
            SELECT COUNT(DISTINCT c.sku.id) 
            FROM Cargo c 
            WHERE c.status = :status
            """)
    Page<SkuAvailabilityResponse> findAvailableSkuStats(
            @Param("status") Status status,
            Pageable pageable
    );

    /**
     * Получение детального пагинированного списка конкретных грузов для указанного SKU.
     * Использование JOIN FETCH предотвращает проблему N+1 при загрузке связанных сущностей на диске.
     */
    @Query(value = """
            SELECT c 
            FROM Cargo c
            JOIN FETCH c.sku
            JOIN FETCH c.dimension
            JOIN FETCH c.location l
            JOIN FETCH l.address
            WHERE c.sku.id = :skuId AND c.status = :status
            """,
            countQuery = """
            SELECT COUNT(c) 
            FROM Cargo c 
            WHERE c.sku.id = :skuId AND c.status = :status
            """)
    Page<Cargo> findBySkuIdAndStatus(
            @Param("skuId") Long skuId,
            @Param("status") Status status,
            Pageable pageable
    );

    // ===

    // Используем ПРОЕКЦИЮ для облегчения возвращаемой сущности, ТОЛЬКО НЕОБХОДИМЫЕ ПОЛЯ!
    @Query("""
           SELECT DISTINCT c.id as id, 
                  c.price as price,
                  c.weight as weight,
                  c.status as status
           FROM Cargo c
           WHERE c.id IN (:cargoIds) 
                AND c.status = :status
           ORDER BY c.id
           """)
    List<CargoReservationProjection> findProjectionsByCargoIdAndStatus(
            @Param("cargoIds") List<Long> cargoIds,
            @Param("status") Status status,
            Pageable pageable
    );

    @Query("""
       SELECT c.id as id, 
              c.price as price, 
              c.weight as weight, 
              c.status as status
       FROM Cargo c
       WHERE c.sku.id = :skuId 
         AND c.status = :status
         AND c.id NOT IN (:excludedIds)
       ORDER BY c.id
       """)
    List<CargoReservationProjection> findProjectionsBySkuIdAndStatusExcludingIds(
            @Param("skuId") Long skuId,
            @Param("status") Status status,
            @Param("excludedIds") List<Long> excludedIds,
            Pageable pageable
    );

    // Используем ПРОЕКЦИЮ для облегчения возвращаемой сущности, ТОЛЬКО НЕОБХОДИМЫЕ ПОЛЯ!
    @Query("""
           SELECT c.id as id, 
                  c.price as price, 
                  c.weight as weight, 
                  c.status as status
           FROM Cargo c
           WHERE c.sku.id = :skuId AND c.status = :status
           ORDER BY c.id
           """)
    List<CargoReservationProjection> findProjectionsBySkuIdAndStatus(
            @Param("skuId") Long skuId,
            @Param("status") Status status,
            Pageable pageable
    );

    // Bulk-обновление статусов
    @Modifying(clearAutomatically = true)  // ← указывает, что запрос изменяет данные, Без неё Hibernate не выполнит запрос. очищает кэш после обновления
    @Query("UPDATE Cargo c SET c.status = :newStatus WHERE c.id IN :ids")
    int updateStatusByIds(
            @Param("ids") List<Long> ids,
            @Param("newStatus") Status newStatus
    );
    // Возвращает количество обновлённых строк






    /**
     * Получить статистику по SKU с группировкой.
     * Здесь мы работаем с Cargo, а не с Sku!
     */
    @Query("""
           SELECT new com.innowise.logistics.cargoservice.dto.SkuStats(
               c.sku.id,
               c.sku.name,
               c.sku.description,
               c.sku.isActive,
               c.sku.createdAt,
               c.sku.updatedAt,
               c.name,
               c.category,
               c.weight,
               c.dimension,
               MIN(c.price),
               MAX(c.price),
               COUNT(c.id),
               MIN(c.createdAt),
               MAX(c.createdAt)
           )
           FROM Cargo c
           WHERE c.status = :status
           AND (:isActive IS NULL OR c.sku.isActive = :isActive)
           GROUP BY 
               c.sku.id,
               c.sku.name,
               c.sku.description,
               c.sku.isActive,
               c.sku.createdAt,
               c.sku.updatedAt,
               c.name,
               c.category,
               c.weight,
               c.dimension
           """)
    Page<SkuStats> findSkuStatsByStatusAndActive(
            @Param("status") Status status,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    /**
     * Получить статистику для конкретных SKU (для пост-обработки)
     */
    @Query("""
           SELECT new com.innowise.logistics.cargoservice.dto.SkuStats(
               c.sku.id,
               c.sku.name,
               c.sku.description,
               c.sku.isActive,
               c.sku.createdAt,
               c.sku.updatedAt,
               c.name,
               c.category,
               c.weight,
               c.dimension,
               MIN(c.price),
               MAX(c.price),
               COUNT(c.id),
               MIN(c.createdAt),
               MAX(c.createdAt)
           )
           FROM Cargo c
           WHERE c.sku.id IN :skuIds
           AND c.status = :status
           GROUP BY 
               c.sku.id,
               c.sku.name,
               c.sku.description,
               c.sku.isActive,
               c.sku.createdAt,
               c.sku.updatedAt,
               c.name,
               c.category,
               c.weight,
               c.dimension
           """)
    List<SkuStats> getStatsBySkuIdsAndStatus(
            @Param("skuIds") List<Long> skuIds,
            @Param("status") Status status
    );
}

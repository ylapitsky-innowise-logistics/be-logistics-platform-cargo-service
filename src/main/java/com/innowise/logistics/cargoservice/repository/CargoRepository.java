package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;  // ✅ ПРАВИЛЬНО
import java.util.List;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Long> {

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
}
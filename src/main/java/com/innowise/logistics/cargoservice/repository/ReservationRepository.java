// ReservationItemRepository
package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

//    /**
//     * Найти все активные бронирования (isActive = true)
//     * с пагинацией.
//     *
//     * @param pageable параметры пагинации
//     * @return страница активных бронирований
//     */
//    @Query("SELECT r FROM Reservation r WHERE r.isActive = true")
//    Page<Reservation> findAllActive(Pageable pageable);
//
//    /**
//     * Найти все НЕактивные бронирования (isActive = false)
//     * с пагинацией.
//     *
//     * @param pageable параметры пагинации
//     * @return страница неактивных бронирований
//     */
//    @Query("SELECT r FROM Reservation r WHERE r.isActive = false")
//    Page<Reservation> findAllInactive(Pageable pageable);

    /**
     * Найти все бронирования по статусу активности.
     * Если isActive = null — вернуть все.
     *
     * @param isActive статус активности (true/false)
     * @param pageable параметры пагинации
     * @return страница бронирований
     */
    @Query("SELECT r FROM Reservation r WHERE (:isActive IS NULL OR r.isActive = :isActive)")
    Page<Reservation> findAllByActive(@Param("isActive") Boolean isActive, Pageable pageable);
}
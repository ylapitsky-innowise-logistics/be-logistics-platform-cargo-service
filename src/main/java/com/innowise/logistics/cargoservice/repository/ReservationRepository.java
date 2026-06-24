// ReservationItemRepository
package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
//    Optional<Reservation> findByOrderId(UUID orderId);
//
//    List<Reservation> findByReservationStatusAndExpiresAtBefore(ReservationStatus status, Instant now);
//
//    @Modifying
//    @Query("UPDATE Reservation r SET r.reservationStatus = :newStatus, r.updatedAt = :now WHERE r.orderId = :orderId")
//    int updateStatusByOrderId(@Param("orderId") UUID orderId,
//                              @Param("newStatus") ReservationStatus newStatus,
//                              @Param("now") Instant now);
//}

}

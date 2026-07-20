package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    // Ищем локацию по координатам + ID адреса
    Optional<Location> findByRackAndShelfAndAddress_Id(String rack, String shelf, Long addressId);
}

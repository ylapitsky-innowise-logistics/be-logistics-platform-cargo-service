package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}

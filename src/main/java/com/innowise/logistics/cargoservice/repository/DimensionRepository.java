package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DimensionRepository extends JpaRepository<Dimension, Long> {

    // Ищем габариты по трём параметрам
    Optional<Dimension> findByLengthAndWidthAndHeight(Double length, Double width, Double height);
}

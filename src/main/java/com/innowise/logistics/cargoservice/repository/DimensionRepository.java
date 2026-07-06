package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DimensionRepository extends JpaRepository<Dimension, Long> {
}

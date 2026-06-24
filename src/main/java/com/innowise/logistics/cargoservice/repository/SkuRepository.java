package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkuRepository extends JpaRepository<Sku, Long> {
}

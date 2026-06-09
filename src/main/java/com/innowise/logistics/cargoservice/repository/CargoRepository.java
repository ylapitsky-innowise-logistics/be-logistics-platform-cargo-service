package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Long> {
    // Стандартного метода findAll(Pageable pageable) уже достаточно для пагинации!
}

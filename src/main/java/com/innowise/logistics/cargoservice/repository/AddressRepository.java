package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}

package com.innowise.logistics.cargoservice.repository;

import com.innowise.logistics.cargoservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // Ищем адрес по всем полям (кроме id)
    Optional<Address> findByCountryAndZipCodeAndCityAndMicrodistrictAndStreetAndHouseAndBlockAndApartment(
            String country,
            String zipCode,
            String city,
            String microdistrict,
            String street,
            Integer house,
            String block,
            String apartment
    );
}

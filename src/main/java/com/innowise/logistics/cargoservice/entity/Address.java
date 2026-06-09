package com.innowise.logistics.cargoservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "id")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @Column(name = "country", nullable = false)
    private String country;         // Страна

    @Column(name = "zip_code", nullable = false)
    private String zipCode;         // Почтовый индекс

    @Column(name = "city", nullable = false)
    private String city;            // Город

    @Column(name = "microdistrict")
    private String microdistrict;   // Микрорайон

    @Column(name = "street")
    private String street;          // Улица

    @Column(name = "house", nullable = false)
    private Integer house;          // Номер дома

    @Column(name = "block")
    private String block;           // Корпус

    @Column(name = "apartment")
    private String apartment;       // Квартира
}

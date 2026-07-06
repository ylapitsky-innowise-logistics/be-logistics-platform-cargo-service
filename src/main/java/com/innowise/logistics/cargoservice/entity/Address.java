package com.innowise.logistics.cargoservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Строгий контроль бизнес-ключа
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @Column(name = "country", nullable = false, length = 50)
    @Comment("Страна")
    @EqualsAndHashCode.Include
    private String country;

    @Column(name = "zip_code", nullable = false, length = 10)
    @Comment("Почтовый индекс")
    @EqualsAndHashCode.Include
    private String zipCode;

    @Column(name = "city", nullable = false, length = 50)
    @Comment("Город")
    @EqualsAndHashCode.Include
    private String city;

    @Column(name = "microdistrict", length = 100)
    @Comment("Микрорайон")
    @EqualsAndHashCode.Include
    private String microdistrict;

    @Column(name = "street", length = 150)
    @Comment("Улица")
    @EqualsAndHashCode.Include
    private String street;

    @Column(name = "house", nullable = false)
    @Comment("Номер дома")
    @EqualsAndHashCode.Include
    private Integer house;

    @Column(name = "block", length = 20)
    @Comment("Корпус")
    @EqualsAndHashCode.Include
    private String block;

    @Column(name = "apartment", length = 20)
    @Comment("Квартира")
    @EqualsAndHashCode.Include
    private String apartment;
}

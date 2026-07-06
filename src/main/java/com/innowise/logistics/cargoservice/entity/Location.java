package com.innowise.logistics.cargoservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Спасает от LazyInitializationException
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;

    @Column(name = "rack", nullable = false, length = 50)
    @Comment("Стеллаж, где находится товар")
    @EqualsAndHashCode.Include // Сравниваем по координатам стеллажа
    private String rack;

    @Column(name = "shelf", length = 50)
    @Comment("Полка, где находится товар")
    @EqualsAndHashCode.Include // и полки
    private String shelf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    @Comment("Внешний ключ на адрес склада (FK)")
    // Аннотацию @EqualsAndHashCode.Include сюда НЕ СТАВИМ, чтобы держать связь ленивой!
    private Address address;
}

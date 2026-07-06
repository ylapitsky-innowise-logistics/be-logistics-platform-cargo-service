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
@Table(name = "dimensions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Исключаем ID из сравнения намертво
public class Dimension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dimension_id")
    private Long id;

    @Column(name = "length", nullable = false)
    @Comment("Длина товара")
    @EqualsAndHashCode.Include
    private Double length;

    @Column(name = "width", nullable = false)
    @Comment("Ширина товара")
    @EqualsAndHashCode.Include
    private Double width;

    @Column(name = "height", nullable = false)
    @Comment("Высота товара")
    @EqualsAndHashCode.Include
    private Double height;
}

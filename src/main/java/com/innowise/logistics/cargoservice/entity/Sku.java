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

@Entity
@Table(name = "skus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sku_id")
    private Long id;

    /**
     * Наименование/код артикула товара
     * Пример: M-0451-2026,
     * где: M — мебель, 0451 — номер модели, 2026 — год
     */
    @Column(name = "name", nullable = false)
    private String name;

//    @Override
//    public String toString() {
//        return  "id=" + id + '\n' +
//                "name='" + name + '\n';
//    }
}

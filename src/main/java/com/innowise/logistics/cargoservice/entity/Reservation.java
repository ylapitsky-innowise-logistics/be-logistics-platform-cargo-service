package com.innowise.logistics.cargoservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @Column(name = "cargo_ids")
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Перечень забронированных товаров")
    private Set<Long> cargoIds;

    @Column(name = "created_at", updatable = false, nullable = false)
    @Setter(AccessLevel.NONE)                           // Отмен. Lombok сеттер. Задается только при создании.
    @Comment("Дата создания брони")
    private Instant createdAt = Instant.now();

    @Column(name = "is_active")
    @Comment("Активна ли бронь на данный момент или отменена")
    private Boolean isActive = true;



    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "total_weight", nullable = false)
    private Double totalWeight;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
}

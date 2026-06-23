package com.innowise.logistics.cargoservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Сущность SKU (Stock Keeping Unit) — Каталожный артикул товара.
 * Описывает модель товара в системе.
 * Бизнес-правило равенства: артикулы считаются ОДИНАКОВЫМИ, если совпадают их уникальные имена/коды (name).
 */
@Entity
@Table(
        name = "skus",
        uniqueConstraints = @UniqueConstraint(name = "uq_sku_name", columnNames = "name") // Название артикула должно быть уникальным на уровне БД
)
@EntityListeners(AuditingEntityListener.class)      // Включаем аудит (создание/изменение)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)   // Строгий контроль equals/hashCode
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sku_id")                        // Уникальность технического ключа (PRIMARY KEY)
    private Long id;

    /**
     * Наименование/код артикула товара
     * Пример: M-0451-2026 (M — мебель, 0451 — номер модели, 2026 — год)
     * */
    @Column(name = "name", nullable = false, length = 100)
    @Comment("Уникальный код/название артикула")
    @EqualsAndHashCode.Include                      // Бизнес-ключ: равенство проверяем ТОЛЬКО по коду артикула
    @Setter(AccessLevel.NONE)                       // Отключаем дефолтный сеттер Lombok для этого поля
    private String name;

    // Кастомный сеттер с защитой от пробелов и null
    public void setName(String name) {
        this.name = (name != null) ? name.trim() : null;
    }

    @Column(name = "description", length = 1000)
    @Comment("Полное описание модели товара для каталога")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Comment("Флаг активности артикула (доступен ли для логистики/закупок)")
    private boolean isActive = true;


    // =========================================================
    // ПОЛЯ АУДИТА (Управляются Spring Data JPA автоматически)
    // =========================================================

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    @Setter(AccessLevel.NONE)
    @Comment("Дата заведения артикула в систему")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @Setter(AccessLevel.NONE)
    @Comment("Дата последнего редактирования данных артикула (любое изменение)")
    private Instant updatedAt;
}

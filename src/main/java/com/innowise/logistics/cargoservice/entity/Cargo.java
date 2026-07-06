package com.innowise.logistics.cargoservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

/**
 Принимаем следующее бизнес-правило: товары считаются ОДИНАКОВЫМИ, если:
    🔴 Должны СОВПАДАТЬ в рамках одного SKU
    Эти параметры описывают природу товара. Если они изменятся, это будет уже совершенно другой товар, и логисты заведут для него новый SKU.
    - name          (Наименование товара)
    - category      (Категория)
    - dimension     (Габаритные размеры)
    - weight        (Вес)

    🟢 Могут и БУДУТ ОТЛИЧАТЬСЯ в рамках одного SKU (но товары все равно будем считать одинаковыми)
    Эти поля описывают свойства конкретной физической единицы груза (паллеты, коробки), её жизненный цикл и её текущее положение в пространстве.
    - location (Место нахождения)
    - status и status_at (Статус)
    - created_at (Дата поступления)
    - price (Стоимость)
 **/
@Entity
@Table(name = "cargos")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)   // Говорим Lombok: сравнивать ТОЛЬКО то, что помечено ниже
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cargo_id")
    private Long id;

    // =========================================================
    // НИЖЕ ПОЛЯ, КОТОРЫЕ УЧАСТВУЮТ В РАВЕНСТВЕ (НЕИЗМЕННЫ)
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)              // LAZY - для производительности
    @JoinColumn(name = "sku_id", nullable = false)  // sku_id - колонка внешнего ключа (FK) в таблице cargos
    @Comment("Артикул товара")
    @EqualsAndHashCode.Include                      // Участвует в бизнес-ключе
    private Sku sku;

    @Column(name = "name", nullable = false)
    @Comment("Наименование товара")
    @EqualsAndHashCode.Include                      // Участвует в бизнес-ключе
    private String name;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
//    @Enumerated(EnumType.STRING)
//    @Column(name = "cargo_category", nullable = false)
    @Column(name = "cargo_category", nullable = false, columnDefinition = "cargo_category_enum")
    @Comment("Категория товара (электроника, книги, спорт...)")
    @EqualsAndHashCode.Include                      // Участвует в бизнес-ключе
    private Category category;

    @Column(name = "weight", nullable = false)
    @Comment("Вес")
    @EqualsAndHashCode.Include                      // Участвует в бизнес-ключе
    private Double weight;                          // в килограммах

    @ManyToOne(fetch = FetchType.LAZY)              // Неск. товаров могут один и тот-же габаритный размер
    @JoinColumn(name = "dimension_id", nullable = false)
    @Comment("Габаритные размеры")
    @EqualsAndHashCode.Include                      // Участвует в бизнес-ключе
    private Dimension dimension;

    // =========================================================
    // НИЖЕ ПОЛЯ, КОТОРЫЕ НЕ УЧАСТВУЮТ В РАВЕНСТВЕ (МЕНЯЮТСЯ)
    // =========================================================

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    @Comment("Стоимость в местной валюте")
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)              // Неск. товаров могут лежать на одной полке
    @JoinColumn(name = "location_id", nullable = false)
    @Comment("Место нахождения товара")
    private Location location;


    @CreatedDate                                    // 🟢 Автоматически выставит текущее время при сохранении через репозиторий
    @Column(name = "created_at", updatable = false, nullable = false)
    @Setter(AccessLevel.NONE)                       // Отмен. Lombok сеттер. Задается только при создании.
    @Comment("Дата поступления на склад")
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
//    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "cargo_status_enum" // Явно указываем схему и имя типа в Postgres
    )
    @Setter(AccessLevel.NONE)                       // Отмен. Lombok сеттер. Только через updateStatus(..)
    @Comment("Текущий статус")
    private Status status = Status.AVAILABLE;

    @Column(name = "status_at", nullable = false)
    @Setter(AccessLevel.NONE)                       // Отмен. Lombok сеттер. Только через updateStatus(..)
    @Comment("Дата последнего изменения статуса")
    private Instant statusAt = Instant.now();       // Это защитит от NULL при первом инсерте


    // Обновление статуса и времени его установки - только через updateStatus(..)
    public void updateStatus(Status newStatus) {
        this.status = newStatus;
        this.statusAt = Instant.now();
    }
}

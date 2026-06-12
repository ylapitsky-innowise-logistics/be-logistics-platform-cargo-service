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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

/**
 Принимаем следующее бизнес-правило: товары считаются ОДИНАКОВЫМИ, если:
🔴 Должны СОВПАДАТЬ в рамках одного SKU
Эти параметры описывают природу товара. Если они изменятся, это будет уже совершенно другой товар, и логисты заведут для него новый SKU.
    - name          (Наименование товара)
    - category      (Категория)
    - dimension     (Габбаритные размеры)
    - mongoDocId    (Фотография товара)
    - weight        (Вес)

🟢 Могут и БУДУТ ОТЛИЧАТЬСЯ в рамках одного SKU (но товары всеравно будем считать одинаковыми)
Эти поля описывают свойства конкретной физической единицы груза (паллеты, коробки), её жизненный цикл и её текущее положение в пространстве.
    - location (Место нахождения)
    - status и status_at (Статус)
    - created_at (Дата поступления)
    - price (Стоимость)
 **/
@Entity
@Table(name = "cargos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
//@EqualsAndHashCode(exclude = "id")
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)   // Говорим Lombok: сравнивать ТОЛЬКО то, что помечено ниже
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cargo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)              // LAZY - для производительности
    @JoinColumn(name = "sku_id", nullable = false)  // sku_id - колонка внешнего ключа (FK) в таблице cargos
    @Comment("Артикул товара")
//    @EqualsAndHashCode.Include                      // Участвует в бизнес-ключе
    private Sku sku;

    @Column(name = "mongo_doc_id")
    @Comment("Фотография товара")
//    @EqualsAndHashCode.Include                      // Участвует в бизнес-ключе
    private String mongoDocId;                      // Линк на MongoDB

    @Column(nullable = false)
    @Comment("Наименование товара")
//    @EqualsAndHashCode.Include                      // Участвует в бизнес-ключе
    private String name;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "cargo_category", nullable = false)
    @Comment("Категория товара (электроника, книги, спорт...)")
//    @EqualsAndHashCode.Include                      // Участвует в бизнес-ключе
    private Category category;

    @Column(name = "weight", nullable = false)
    @Comment("Вес")
//    @EqualsAndHashCode.Include                      // Участвует в бизнес-ключе
    private Double weight;                          // в килограммах

    @ManyToOne(fetch = FetchType.LAZY)              // Неск. товаров могут один и тот-же габаритный размер
    @JoinColumn(name = "dimension_id", nullable = false)
    @Comment("Габаритные размеры")
//    @EqualsAndHashCode.Include                      // Участвует в бизнес-ключе
    private Dimension dimension;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    @Comment("Стоимость в местной валюте")
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)              // Неск. товаров могут лежать на одной полке
    @JoinColumn(name = "location_id", nullable = false)
    @Comment("Место нахождения товара")
    private Location location;


    @Column(name = "created_at", updatable = false, nullable = false)
    @Setter(AccessLevel.NONE)                       // Отмен. Lombok сеттер. Задается только при создании.
    @Comment("Дата поступления на склад")
    private Instant createdAt = Instant.now();

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "cargo_schema.cargo_status_enum" // Явно указываем схему и имя типа в Postgres
    )
    @Setter(AccessLevel.NONE)                       // Отмен. Lombok сеттер. Только через updateStatus(..)
    @Comment("Текущий статус")
    private Status status = Status.AVAILABLE;

    @Column(name = "status_at", nullable = false)
    @Setter(AccessLevel.NONE)                       // Отмен. Lombok сеттер. Только через updateStatus(..)
    @Comment("Дата последнего изменения статуса")
    private Instant statusAt = Instant.now();


    // Обновление статуса и времени его установки - только через updateStatus(..)
    public void updateStatus(Status newStatus) {
        this.status = newStatus;
        this.statusAt = Instant.now();
    }
}

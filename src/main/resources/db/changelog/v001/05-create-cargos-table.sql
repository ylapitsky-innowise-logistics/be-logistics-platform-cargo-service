--liquibase formatted sql

--changeset Yury Lapitski:2026-06-09-005
--comment: Создание основной таблицы грузов (товаров на складе) с использованием нативных ENUM PostgreSQL

CREATE TYPE cargo_category_enum AS ENUM ('ELECTRONICS', 'BOOKS', 'SPORTS', 'OTHER');
CREATE TYPE cargo_status_enum AS ENUM ('AVAILABLE', 'RESERVED', 'SHIPPED');

CREATE TABLE cargos(
    cargo_id       BIGSERIAL,
    sku_id         BIGINT                   NOT NULL,
    name           VARCHAR(255)             NOT NULL,
    cargo_category cargo_category_enum      NOT NULL DEFAULT 'OTHER',
    weight         DOUBLE PRECISION         NOT NULL,
    dimension_id   BIGINT                   NOT NULL,
    price          NUMERIC(19, 2)           NOT NULL,
    location_id    BIGINT                   NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    status         cargo_status_enum        NOT NULL DEFAULT 'AVAILABLE',
    status_at      TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_cargos PRIMARY KEY (cargo_id),
    CONSTRAINT fk_cargos_on_sku FOREIGN KEY (sku_id) REFERENCES skus (sku_id),
    CONSTRAINT fk_cargos_on_dimension FOREIGN KEY (dimension_id) REFERENCES dimensions (dimension_id),
    CONSTRAINT fk_cargos_on_location FOREIGN KEY (location_id) REFERENCES locations (location_id)
);

COMMENT ON COLUMN cargos.cargo_id IS 'Первичный ключ груза';
COMMENT ON COLUMN cargos.sku_id IS 'Внешний ключ на артикул товара (FK)';
COMMENT ON COLUMN cargos.name IS 'Наименование товара';
COMMENT ON COLUMN cargos.cargo_category IS 'Категория товара (электроника, книги, спорт...)';
COMMENT ON COLUMN cargos.weight IS 'Вес груза';
COMMENT ON COLUMN cargos.dimension_id IS 'Внешний ключ на габаритные размеры (FK)';
COMMENT ON COLUMN cargos.price IS 'Стоимость в местной валюте';
COMMENT ON COLUMN cargos.location_id IS 'Внешний ключ на складскую ячейку (FK)';
COMMENT ON COLUMN cargos.created_at IS 'Дата и время поступления на склад';
COMMENT ON COLUMN cargos.status IS 'Текущий статус (AVAILABLE, RESERVED, SHIPPED)';
COMMENT ON COLUMN cargos.status_at IS 'Дата последнего изменения статуса';

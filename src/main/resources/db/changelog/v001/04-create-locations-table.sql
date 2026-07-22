--liquibase formatted sql

--changeset Yury Lapitski:2026-06-09-004
--comment: Создание таблицы складских ячеек хранения

CREATE TABLE locations(
    location_id BIGSERIAL,
    shelf       VARCHAR(50),
    rack        VARCHAR(50) NOT NULL,
    address_id  BIGINT      NOT NULL,

    CONSTRAINT pk_locations PRIMARY KEY (location_id),
    CONSTRAINT fk_locations_on_address FOREIGN KEY (address_id) REFERENCES addresses (address_id)
);

-- ✅ Уникальность ячейки по координатам + ID адреса
ALTER TABLE locations ADD CONSTRAINT uq_locations_rack_shelf_address UNIQUE (rack, shelf, address_id);

COMMENT ON COLUMN locations.location_id IS 'Первичный ключ ячейки нахождения';
COMMENT ON COLUMN locations.shelf IS 'Полка, где находится товар';
COMMENT ON COLUMN locations.rack IS 'Стеллаж, где находится товар';
COMMENT ON COLUMN locations.address_id IS 'Внешний ключ на адрес склада (FK)';

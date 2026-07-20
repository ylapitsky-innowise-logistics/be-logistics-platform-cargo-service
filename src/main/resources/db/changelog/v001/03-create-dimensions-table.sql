--liquibase formatted sql

--changeset Yury Lapitski:2026-06-09-003
--comment: Создание таблицы габаритных размеров

CREATE TABLE dimensions(
    dimension_id BIGSERIAL,
    length       DOUBLE PRECISION NOT NULL,
    width        DOUBLE PRECISION NOT NULL,
    height       DOUBLE PRECISION NOT NULL,

    CONSTRAINT pk_dimensions PRIMARY KEY (dimension_id)
);

ALTER TABLE dimensions ADD CONSTRAINT uq_dimensions_length_width_height UNIQUE (length, width, height);

COMMENT ON COLUMN dimensions.dimension_id IS 'Первичный ключ габаритов';
COMMENT ON COLUMN dimensions.length IS 'Длина товара';
COMMENT ON COLUMN dimensions.width IS 'Ширина товара';
COMMENT ON COLUMN dimensions.height IS 'Высота товара';

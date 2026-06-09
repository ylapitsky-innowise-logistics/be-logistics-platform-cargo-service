--liquibase formatted sql

--changeset Yury Lapitski:2026-06-09-002
--comment: Создание таблицы артикулов (SKU)

CREATE TABLE skus(
    sku_id BIGSERIAL,
    name   VARCHAR(255) NOT NULL,

    CONSTRAINT pk_skus PRIMARY KEY (sku_id)
);

COMMENT ON COLUMN skus.sku_id IS 'Первичный ключ артикула';
COMMENT ON COLUMN skus.name IS 'Наименование/код артикула товара';

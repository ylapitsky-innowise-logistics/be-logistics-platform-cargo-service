--liquibase formatted sql

--changeset Yury Lapitski:2026-06-09-002
--comment: Создание таблицы артикулов (SKU)

CREATE TABLE skus(
    sku_id      BIGSERIAL,
    name        VARCHAR(100)    NOT NULL,

    description VARCHAR(1000),
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL,

    CONSTRAINT pk_skus PRIMARY KEY (sku_id),
    CONSTRAINT uq_sku_name UNIQUE (name) -- Гарантия уникальности бизнес-кода на уровне БД
);

COMMENT ON COLUMN skus.sku_id IS 'Первичный ключ артикула';
COMMENT ON COLUMN skus.name IS 'Уникальный код/название артикула (бизнес-ключ)';
COMMENT ON COLUMN skus.description IS 'Полное описание модели товара для каталога';
COMMENT ON COLUMN skus.is_active IS 'Флаг активности артикула (доступен ли для логистики/закупок)';
COMMENT ON COLUMN skus.created_at IS 'Дата заведения артикула в систему';
COMMENT ON COLUMN skus.updated_at IS 'Дата последнего редактирования данных артикула';

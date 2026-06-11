--liquibase formatted sql

--changeset Yury Lapitski:2026-06-10-001
--comment: Создание таблицы бронирований

CREATE TABLE reservations(
    reservation_id  BIGSERIAL,
    cargo_ids       JSONB               NOT NULL,
    created_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    is_active       BOOLEAN             NOT NULL DEFAULT TRUE,

    total_price     NUMERIC(19, 2)      NOT NULL,
    total_weight    DOUBLE PRECISION    NOT NULL,
    total_quantity  INTEGER             NOT NULL,
    currency        VARCHAR(3)          NOT NULL,

    CONSTRAINT pk_reservations PRIMARY KEY (reservation_id)
);

COMMENT ON TABLE reservations IS 'Таблица бронирований товаров';
COMMENT ON COLUMN reservations.reservation_id IS 'Первичный ключ бронирования';
COMMENT ON COLUMN reservations.cargo_ids IS 'Перечень забронированных товаров (массив ID товаров в формате JSON)';
COMMENT ON COLUMN reservations.created_at IS 'Дата создания брони';
COMMENT ON COLUMN reservations.is_active IS 'Активна ли бронь на данный момент или отменена';
COMMENT ON COLUMN reservations.total_price IS 'Общая стоимость бронирования';
COMMENT ON COLUMN reservations.total_weight IS 'Общий вес товаров в брони';
COMMENT ON COLUMN reservations.total_quantity IS 'Общее количество товаров';
COMMENT ON COLUMN reservations.currency IS 'Валюта стоимости (например, USD, EUR, RUB)';

-- Индексы (для работы шедулера)
CREATE INDEX idx_reservations_active_created ON reservations(is_active, created_at);

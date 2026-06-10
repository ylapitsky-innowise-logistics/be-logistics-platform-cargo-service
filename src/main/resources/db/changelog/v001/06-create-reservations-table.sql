--liquibase formatted sql

--changeset Yury Lapitski:2026-06-10-001
--comment: Создание таблицы бронирований

CREATE TABLE reservations(
    booking_id BIGSERIAL,
    cargo_ids  JSONB        NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_reservations PRIMARY KEY (reservation_id)
);

COMMENT ON TABLE reservations IS 'Таблица бронирований товаров';
COMMENT ON COLUMN reservations.reservation_id IS 'Первичный ключ бронирования';
COMMENT ON COLUMN reservations.cargo_ids IS 'Перечень забронированных товаров (массив ID товаров в формате JSON)';
COMMENT ON COLUMN reservations.created_at IS 'Дата создания брони';
COMMENT ON COLUMN reservations.is_active IS 'Активна ли бронь на данный момент или отменена';

-- Индексы (для работы шедулера)
CREATE INDEX idx_reservations_created_at ON reservations(created_at);

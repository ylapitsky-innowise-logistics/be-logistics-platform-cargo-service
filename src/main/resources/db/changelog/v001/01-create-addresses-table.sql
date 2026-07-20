--liquibase formatted sql

--changeset Yury Lapitski:2026-06-09-001
--comment: Создание таблицы адресов склада/клиентов

CREATE TABLE addresses(
    address_id    BIGSERIAL,
    country       VARCHAR(50) NOT NULL,
    zip_code      VARCHAR(10) NOT NULL,
    city          VARCHAR(50) NOT NULL,
    microdistrict VARCHAR(100),
    street        VARCHAR(150),
    house         INTEGER     NOT NULL,
    block         VARCHAR(20),
    apartment     VARCHAR(20),

    CONSTRAINT pk_addresses PRIMARY KEY (address_id)
);

-- ✅ Уникальность адреса по всем полям (кроме ID)
ALTER TABLE addresses ADD CONSTRAINT uq_addresses_full UNIQUE (
    country, zip_code, city, microdistrict, street, house, block, apartment
);

COMMENT ON COLUMN addresses.address_id IS 'Первичный ключ адреса';
COMMENT ON COLUMN addresses.country IS 'Страна';
COMMENT ON COLUMN addresses.zip_code IS 'Почтовый индекс';
COMMENT ON COLUMN addresses.city IS 'Город';
COMMENT ON COLUMN addresses.microdistrict IS 'Микрорайон';
COMMENT ON COLUMN addresses.street IS 'Улица';
COMMENT ON COLUMN addresses.house IS 'Номер дома';
COMMENT ON COLUMN addresses.block IS 'Корпус';
COMMENT ON COLUMN addresses.apartment IS 'Квартира';

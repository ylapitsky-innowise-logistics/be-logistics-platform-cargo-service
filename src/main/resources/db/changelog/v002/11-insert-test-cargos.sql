--liquibase formatted sql

--changeset Yury Lapitski:2026-06-09-010-test-data
--comment: Генерация 1000 товаров без использования PL/pgSQL блоков

INSERT INTO cargos (
    sku_id,
    mongo_doc_id,
    name,
    cargo_category,
    weight,
    dimension_id,
    price,
    location_id,
    created_at,
    status,
    status_at
)
SELECT
    (floor(random() * 20) + 1)::BIGINT AS sku_id,
    'https://mongodb.internal/storage/docs/img_' || (floor(random() * 10000) + 1000)::INT || '.jpg' AS mongo_doc_id,
    'Товар #' || id AS name,
    (ARRAY['ELECTRONICS', 'BOOKS', 'SPORTS', 'OTHER'])[floor(random() * 4) + 1]::cargo_category_enum AS cargo_category,
    (random() * 45 + 0.1)::DOUBLE PRECISION AS weight,
    (floor(random() * 20) + 1)::BIGINT AS dimension_id,
    (random() * 1500 + 10)::NUMERIC(19,2) AS price,
    (floor(random() * 100) + 1)::BIGINT AS location_id,
    NOW() - (random() * interval '30 days') AS created_at,
    (ARRAY['AVAILABLE', 'RESERVED', 'SHIPPED'])[floor(random() * 3) + 1]::cargo_status_enum AS status,
    NOW() - (random() * interval '28 days') AS status_at
FROM generate_series(1, 1000) AS id;
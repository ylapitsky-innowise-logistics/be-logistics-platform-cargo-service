package com.innowise.logistics.cargoservice.dto.response;

import com.innowise.logistics.cargoservice.entity.Category;
import com.innowise.logistics.cargoservice.entity.Status;
import java.math.BigDecimal;
import java.time.Instant;

public record CargoViewResponse(
        Long id,
        Long skuId,          // Берем из связанной сущности Sku
        String skuName,          // Берем из связанной сущности Sku
        String mongoDocId,
        String name,
        Category category,
        Double weight,
        String dimensions,       // Форматируем красиво: "LxWxH"
        BigDecimal price,
        String location, // Собираем как "Стеллаж / Полка"
        Instant createdAt,
        Status status,
        Instant statusAt
) {}

/*
Пример вывода:
{
  "content": [
    {
      "id": 7,
      "skuId": 4,
      "skuName": "E-7011-2024",
      "mongoDocId": "https://mongodb.internal/storage/docs/img_8992.jpg",
      "name": "Товар #7",
      "category": "SPORTS",
      "weight": 25.735439425149533,
      "dimensions": "22,5x22,5x22,5",
      "price": 16.85,
      "locationLocation": "Стеллаж LT-K / Полка 1",
      "createdAt": "2026-06-07T13:01:16.064713Z",
      "status": "AVAILABLE",
      "statusAt": "2026-05-31T23:22:23.901392Z"
    },
    {
      "id": 8,
      "skuId": 20,
      "skuName": "O-3345-2026",
      "mongoDocId": "https://mongodb.internal/storage/docs/img_1499.jpg",
      "name": "Товар #8",
      "category": "BOOKS",
      "weight": 29.31784136036326,
      "dimensions": "21,0x14,8x3,5",
      "price": 1420.38,
      "locationLocation": "A-12 / 01",
      "createdAt": "2026-06-03T21:23:16.109191Z",
      "status": "AVAILABLE",
      "statusAt": "2026-05-27T17:06:56.968567Z"
    },
    {
      "id": 9,
      "skuId": 8,
      "skuName": "B-0084-2023",
      "mongoDocId": "https://mongodb.internal/storage/docs/img_8864.jpg",
      "name": "Товар #9",
      "category": "SPORTS",
      "weight": 21.9754115245859,
      "dimensions": "120,0x60,0x75,0",
      "price": 767.41,
      "locationLocation": "Zone-US-1 / Level 2",
      "createdAt": "2026-05-14T09:21:52.000287Z",
      "status": "AVAILABLE",
      "statusAt": "2026-05-15T20:21:57.778548Z"
    }
  ],
  "pageable": {
    "pageNumber": 2,
    "pageSize": 3,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 6,
    "paged": true,
    "unpaged": false
  },
  "last": false,
  "totalPages": 334,
  "totalElements": 1000,
  "size": 3,
  "number": 2,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "first": false,
  "numberOfElements": 3,
  "empty": false
}
 */
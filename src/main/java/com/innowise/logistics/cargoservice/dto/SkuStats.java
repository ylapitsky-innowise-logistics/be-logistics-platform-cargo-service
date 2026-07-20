package com.innowise.logistics.cargoservice.dto;

import com.innowise.logistics.cargoservice.entity.Category;
import com.innowise.logistics.cargoservice.entity.Dimension;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO для статистики
 * Используется при получении данных по Sku (артикулам) из CargoRepository,
 * то есть дополнения данными статистики из существующих Cargo
 */
@Getter
@AllArgsConstructor
@ToString
public class SkuStats {
    private Long skuId;
    private String skuName;
    private String skuDescription;
    private boolean skuActive;
    private Instant skuCreatedAt;
    private Instant skuUpdatedAt;

    // Поля из Cargo (актуальные характеристики)
    private String cargoName;
    private Category category;
    private Double weight;
    private Dimension dimension;

    // Агрегированные данные
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Long availableQuantity;
    private Instant createdAtFrom;
    private Instant createdAtTo;
}
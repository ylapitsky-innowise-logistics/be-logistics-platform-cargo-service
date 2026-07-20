package com.innowise.logistics.cargoservice.mapper;

import com.innowise.logistics.cargoservice.dto.SkuStats;
import com.innowise.logistics.cargoservice.dto.response.SkuAvailabilityResponse;
import com.innowise.logistics.cargoservice.entity.Sku;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SkuAvailabilityMapper {

    /**
     * Маппинг SkuStats -> SkuAvailabilityResponse
     */
    public SkuAvailabilityResponse toResponse(SkuStats stats) {
        // Собираем Sku из данных статистики
        Sku sku = new Sku();
        sku.setId(stats.getSkuId());
        sku.setName(stats.getSkuName());
        sku.setDescription(stats.getSkuDescription());
        sku.setActive(stats.isSkuActive());
//        sku.setCreatedAt(stats.getSkuCreatedAt());
//        sku.setUpdatedAt(stats.getSkuUpdatedAt());

        return new SkuAvailabilityResponse(
                sku,
                stats.getCargoName(),
                stats.getCategory(),
                stats.getWeight(),
                stats.getDimension(),
                stats.getPriceMin() != null ? stats.getPriceMin() : BigDecimal.ZERO,
                stats.getPriceMax() != null ? stats.getPriceMax() : BigDecimal.ZERO,
                stats.getCreatedAtFrom(),
                stats.getCreatedAtTo(),
                stats.getAvailableQuantity() != null ? stats.getAvailableQuantity() : 0L
        );
    }

    /**
     * Маппинг с дефолтными значениями
     */
    public SkuAvailabilityResponse toResponse(Sku sku, SkuStats stats) {
        return new SkuAvailabilityResponse(
                sku,
                stats != null ? stats.getCargoName() : sku.getName(),
                stats != null ? stats.getCategory() : null,
                stats != null ? stats.getWeight() : null,
                stats != null ? stats.getDimension() : null,
                stats != null ? stats.getPriceMin() : BigDecimal.ZERO,
                stats != null ? stats.getPriceMax() : BigDecimal.ZERO,
                stats != null ? stats.getCreatedAtFrom() : sku.getCreatedAt(),
                stats != null ? stats.getCreatedAtTo() : sku.getUpdatedAt(),
                stats != null ? stats.getAvailableQuantity() : 0L
        );
    }
}
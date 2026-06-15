package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.response.CargoViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.innowise.logistics.cargoservice.dto.response.SkuAvailabilityResponse;
import com.innowise.logistics.cargoservice.dto.response.SkuResponse;
import com.innowise.logistics.cargoservice.service.CargoService;
import com.innowise.logistics.cargoservice.service.SkuService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/catalog/skus")
@RequiredArgsConstructor
public class SkuController {

    private final CargoService cargoService;
    private final SkuService skuService;

    /**
     * 1️⃣ GET /api/v1/catalog/skus
     * Просмотр агрегированной статистики по всем УНИКАЛЬНЫХ доступным SKU.
     * (просмотр уникальных товаров)
     */
    @GetMapping
    public ResponseEntity<PageResponse<SkuAvailabilityResponse>> getAvailableSkus(
            @PageableDefault(page = 0, size = 10, sort = "sku.id") Pageable pageable) {

        log.info("REST запрос на получение агрегированной статистики SKU. Пагинация: {}", pageable);
        Page<SkuAvailabilityResponse> response = skuService.getAvailableSkus(pageable);
        return ResponseEntity.ok(PageResponse.from(response));
    }

    /**
     * 2️⃣ GET /api/v1/catalog/skus/{skuId}/items
     * Получить детальный пагинированный список конкретных доступных грузов по ID артикула.
     * Поддерживает гибкую кастомною сортировку (8 видов) через текстовый параметр sortBy.
     */
    @GetMapping("/{skuId}/items")
    public ResponseEntity<PageResponse<CargoViewResponse>> getAvailableItemsBySku(
            @PathVariable @Positive(message = "ID артикула должен быть целым положительным числом") Long skuId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Номер страницы не может быть отрицательным") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Размер страницы должен быть не менее 1")
            @Max(value = 100, message = "Размер страницы не может превышать 100 товаров на 1 страницу") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        log.info("REST запрос на детальные товары для SKU ID: {}. Страница: {}, Размер: {}, Сортировка: {}",
                skuId, page, size, sortBy);

        Page<CargoViewResponse> response = skuService.getAvailableItemsBySku(skuId, page, size, sortBy);
        return ResponseEntity.ok(PageResponse.from(response));
    }

    /**
     * 3️⃣ GET /api/v1/catalog/skus/base
     * Получить плоский пагинированный список абсолютно всех зарегистрированных в системе SKU (артикулов).
     */
    @GetMapping("/base")
    public ResponseEntity<PageResponse<SkuResponse>> getSkus(
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {

        log.info("REST запрос на получение базового списка SKU. Пагинация: {}", pageable);
        Page<SkuResponse> response = skuService.getSkus(pageable);
        return ResponseEntity.ok(PageResponse.from(response));
    }
}

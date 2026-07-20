package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.SkuCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.SkuUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.*;
import com.innowise.logistics.cargoservice.service.SkuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/catalog/skus")
@RequiredArgsConstructor
@Validated
public class SkuController {

    private final SkuService skuService;

    // =========================================================
    // CRUD ОПЕРАЦИИ (В едином стиле архитектуры платформы)
    // =========================================================

    /**
     * 1️⃣ POST /api/v1/catalog/skus
     * C - Create: Заведение нового артикула (SKU) в каталог.
     */
    @PostMapping
    public ResponseEntity<SkuCreatingResponse> createSku(
            @RequestBody @Valid SkuCreatingRequest request) {
        log.info("REST запрос на создание SKU: {}", request);
        SkuCreatingResponse response = skuService.createSku(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 2️⃣ GET /api/v1/catalog/skus/{id}
     * R - Read: Получение полной карточки артикула по его ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SkuViewResponse> getSkuById(
            @PathVariable @Positive(message = "ID артикула должен быть положительным числом") Long id) {
        log.info("REST запрос на получение полной карточки SKU ID: {}", id);
        SkuViewResponse response = skuService.getSkuById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 3️⃣ PUT /api/v1/catalog/skus/{id}
     * U - Update: Изменение параметров существующего артикула по его ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SkuViewResponse> updateSku(
            @PathVariable @Positive(message = "ID артикула должен быть положительным числом") Long id,
            @RequestBody @Valid SkuUpdateRequest request) {
        log.info("REST запрос на обновление SKU ID: {}, данные: {}", id, request);
        SkuViewResponse response = skuService.updateSku(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 4️⃣ DELETE /api/v1/catalog/skus/{id}
     * D - Delete: Полное удаление артикула по его ID из системы.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSku(
            @PathVariable @Positive(message = "ID артикула должен быть положительным числом") Long id) {
        log.warn("REST запрос на удаление SKU ID: {}", id);
        skuService.deleteSku(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // СПЕЦИАЛЬНЫЕ БИЗНЕС-ЭНДПОИНТЫ (СОХРАНЕНЫ ДЛЯ ФРОНТЕНДА)
    // =========================================================

    /**
     * 5️⃣ GET /api/v1/catalog/skus
     * Просмотр агрегированной статистики по всем доступным уникальным артикулам (остатки на складе).
     */
    @GetMapping
    public ResponseEntity<PageResponse<SkuAvailabilityResponse>> getAvailableSkus(
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(page = 0, size = 10, sort = "sku.id") Pageable pageable) {
        log.info("REST запрос на агрегированную статистику SKU. Пагинация: {}", pageable);
        Page<SkuAvailabilityResponse> response = skuService.getAvailableSkusByActive(isActive, pageable);
        return ResponseEntity.ok(PageResponse.from(response));
    }

    /**
     * 6️⃣ GET /api/v1/catalog/skus/{skuId}/items
     * Получить детальный пагинированный список конкретных грузов, принадлежащих этому артикулу.
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
     * 7️⃣ GET /api/v1/catalog/skus/base
     * Получить плоский базовый пагинированный список абсолютно всех артикулов системы.
     */
    @GetMapping("/base")
    public ResponseEntity<PageResponse<SkuResponse>> getSkus(
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        log.info("REST запрос на получение базового списка SKU. Пагинация: {}", pageable);
        Page<SkuResponse> response = skuService.getSkus(pageable);
        return ResponseEntity.ok(PageResponse.from(response));
    }
}

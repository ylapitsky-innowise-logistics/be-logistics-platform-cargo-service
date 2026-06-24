package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.CargoCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.CargoUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.CargoCalculationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoCreatingResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.innowise.logistics.cargoservice.service.CargoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/catalog/cargos")
@RequiredArgsConstructor
@Validated
public class CargoController {

    private final CargoService cargoService;

    // =========================================================
    // CRUD ОПЕРАЦИИ ДЛЯ ТОВАРОВ
    // =========================================================

    /**
     * 1️⃣ POST /api/v1/catalog/cargos
     * C - Create: Оформление поступления новой единицы груза на склад.
     */
    @PostMapping
    public ResponseEntity<CargoCreatingResponse> createCargo(
            @RequestBody @Valid CargoCreatingRequest request) {
        log.info("REST запрос на регистрацию груза: {}", request.getName());
        CargoCreatingResponse response = cargoService.createCargo(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 2️⃣ PUT /api/v1/catalog/cargos/{id}
     * U - Update: Полное обновление параметров существующего груза по ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CargoViewResponse> updateCargo(
            @PathVariable Long id,
            @RequestBody @Valid CargoUpdateRequest request) {
        log.info("REST запрос на изменение данных груза ID: {}, данные: {}", id, request);
        CargoViewResponse response = cargoService.updateCargo(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 3️⃣ DELETE /api/v1/catalog/cargos/{id}
     * D - Delete: Удаление груза из системы по его ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCargo(@PathVariable Long id) {
        log.warn("REST запрос на удаление груза ID: {}", id);
        cargoService.deleteCargo(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 4️⃣ GET /api/v1/catalog/cargos
     * Просмотр агрегированного списка всех доступных товаров.
     */
    @GetMapping
    public ResponseEntity<PageResponse<CargoViewResponse>> getAllCargos(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<CargoViewResponse> page = cargoService.getAllCargos(pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    /**
     * 5️⃣ GET /api/v1/catalog/cargos/{id}
     * Получить выбранный товар по его ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CargoViewResponse> getCatalogItemById(@PathVariable Long id) {
        CargoViewResponse item = cargoService.getCargoById(id);
        return ResponseEntity.ok(item);
    }

    /**
     * 6️⃣ POST /api/v1/catalog/cargos/calculate-price
     * Рассчет стоимости списка товаров по переданному списку их 'id'
     */
    @PostMapping("/calculate-price")
    public ResponseEntity<CargoCalculationResponse> calculatePriceByIds(
            @RequestBody
            @Valid
            @NotEmpty(message = "Список товаров не может быть пустым")
            List<@NotNull (message = "ID товара не может быть = null") Long> cargoIds) {

        CargoCalculationResponse response = cargoService.calculatePriceByIds(cargoIds);
        return ResponseEntity.ok(response);
    }
}

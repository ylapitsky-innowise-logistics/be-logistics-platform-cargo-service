package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.response.CargoCalculationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.innowise.logistics.cargoservice.service.CargoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog/cargos")
@RequiredArgsConstructor
@Validated
public class CargoController {

    private final CargoService cargoService;

    /**
     * 1️⃣ GET /api/v1/catalog/cargos
     * Просмотр агрегированного списка всех доступных товаров.
     */
    @GetMapping
    public ResponseEntity<PageResponse<CargoViewResponse>> getAllCargos(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Page<CargoViewResponse> page = cargoService.getAllCargos(pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    /**
     * 2️⃣ GET /api/v1/catalog/cargos/{id}
     * Получить выбранный товар по его ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CargoViewResponse> getCatalogItemById(
            @PathVariable Long id) {

        CargoViewResponse item = cargoService.getCargoById(id);
        return ResponseEntity.ok(item);
    }

    /**
     * 3️⃣ GET /api/v1/catalog/cargos/calculate-price
     * Рассчет стоимости списка товаров по переданному списку их 'id'
     */
    @PostMapping("/calculate-price")
    public ResponseEntity<CargoCalculationResponse> calculatePriceByIds(
            @RequestBody
            @Valid                                                                          // @Valid на параметре – включает валидацию для @NotEmpty
            @NotEmpty(message = "Список товаров не может быть пустым")                      // @NotEmpty – проверяет (сначала), что список не null и не пуст
            List<@NotNull (message = "ID товара не может быть = null") Long> cargoIds) {    // List<@NotNull Long> – (во 2-ю очередь) при наличии @Valid на параметре будет проверять, что каждый элемент списка не null

        CargoCalculationResponse response = cargoService.calculatePriceByIds(cargoIds);
        return ResponseEntity.ok(response);
    }
}

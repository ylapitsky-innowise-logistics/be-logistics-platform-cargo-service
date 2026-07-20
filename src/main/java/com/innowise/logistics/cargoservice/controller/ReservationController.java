package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.CargoReservationRequest;
import com.innowise.logistics.cargoservice.dto.response.CargoReservationResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.innowise.logistics.cargoservice.service.ReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/catalog/reservations")
@RequiredArgsConstructor
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 1️⃣ POST /api/v1/catalog/reservations
     * Зарезервировать товары
     * согласно переданному списку (Sku / количество, зашито в CargoReservationResponse) .
     */
    @PostMapping("")
    public ResponseEntity<CargoReservationResponse> createReservation(
            @RequestBody
            @Valid
            @NotEmpty(message = "Список резервируемых товаров не может быть пустым")
            List<@NotNull(message = "Запрос по артикулу не может быть = null") CargoReservationRequest> requests) {

        log.info("REST запрос на создание бронирования для {} позиций", requests.size());
        CargoReservationResponse response = reservationService.reserveItems(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2️⃣ PATCH /api/v1/catalog/reservations/{id}/cancel
     * Снять резервирование (бронь) с товаров.
     * Возвращает статус 204 No Content в случае успеха.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable("id") Long id) {

        log.info("REST запрос на отмену бронирования с id = {}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 3️⃣ GET /api/v1/catalog/reservations
     * Получить список бронирований с пагинацией и фильтром по активности.
     * is_active = true -> вернет только активные
     * is_active = false -> вернет только НЕ активные
     * is_active = null -> вернет все
     */
    @GetMapping
    public ResponseEntity<PageResponse<CargoReservationResponse>> getAllReservations(
            @RequestParam(required = false, name = "is_active") Boolean isActive,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("REST запрос на получение бронирований. isActive={}, page={}, size={}",
                isActive, pageable.getPageNumber(), pageable.getPageSize());
        Page<CargoReservationResponse> response = reservationService.getAllReservations(isActive, pageable);
        return ResponseEntity.ok(PageResponse.from(response));
    }

    /**
     * 4️⃣ PATCH /api/v1/catalog/reservations/{id}/ship
     * Перевести забронированные товары в статус отгрузки (SHIPPED).
     * Возвращает статус 204 No Content в случае успешной отгрузки.
     */
    @PatchMapping("/{id}/ship")
    public ResponseEntity<Void> shipReservation(@PathVariable("id") Long id) {
        log.info("REST запрос на отгрузку товаров по бронированию с id = {}", id);
        reservationService.shipReservation(id);
        return ResponseEntity.noContent().build();
    }
}

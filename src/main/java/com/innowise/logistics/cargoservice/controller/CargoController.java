package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.CargoReservationRequest;
import com.innowise.logistics.cargoservice.dto.response.*;
import com.innowise.logistics.cargoservice.service.CargoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    // ########## ########## ##########   ПРОСМОТРЫ  ########## ########## ##########
    // Просмотр поштучного перечня доступных товаров ИЗ БД
    @GetMapping("/items")
    public ResponseEntity<PageResponse<CargoViewResponse>> getCatalogItems(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Page<CargoViewResponse> page = cargoService.getCatalogItems(pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    // Просмотр выбранного из БД товара по его id
    @GetMapping("/items/{id}")
    public ResponseEntity<CargoViewResponse> getCatalogItemById(@PathVariable Long id) {
        CargoViewResponse item = cargoService.getCatalogItemById(id);
        return ResponseEntity.ok(item);
    }

    /*
    GET	/api/v1/catalog/skus - Все SKU с количеством доступных товаров
    GET	/api/v1/catalog/skus/{skuId}/items	skuId — ID артикула	Все товары конкретного SKU
    GET	/api/v1/catalog/skus/grouped-by-category	—	SKU сгруппированные по категориям
    /api/v1/catalog/skus/availability   -   Список SKU
     */

    // 1️⃣ Просмотр списка всех УНИКАЛЬНЫХ товаров из БД
    @GetMapping("/skus")
    public ResponseEntity<PageResponse<SkuAvailabilityResponse>> getAvailableSkus(
            @PageableDefault(size = 10, sort = "sku.id") Pageable pageable) {

        Page<SkuAvailabilityResponse> page = cargoService.getAvailableSkus(pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    // 2️⃣ Все доступные товары по конкретному SKU
    @GetMapping("/skus/{skuId}/items")
    public ResponseEntity<PageResponse<CargoViewResponse>> getAvailableItemsBySku(
        @PathVariable @Positive(message = "ID артикула должен быть целым положительным числом") Long skuId,
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Номер страницы не может быть отрицательным") int page,
        @RequestParam(defaultValue = "10") @Min(value = 1, message = "Размер страницы должен быть не менее 1")
            @Max(value = 100, message = "Размер страницы не может превышать 100 товаров на 1 страницу") int size,
        @RequestParam(defaultValue = "id") String sortBy) {

        Page<CargoViewResponse> responsePage = cargoService.getAvailableItemsBySku(skuId, page, size, sortBy);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }


    // ########## ########## ##########   РАССЧЕТЫ  ########## ########## ##########
    @PostMapping("/items/calculate-price")
    public ResponseEntity<CargoCalculationResponse> calculatePrice(
            @RequestBody
            @Valid                                                                      // @Valid на параметре – включает валидацию для @NotEmpty
            @NotEmpty(message = "Список товаров не может быть пустым")                  // @NotEmpty – проверяет (сначала), что список не null и не пуст
            List<@NotNull (message = "ID товара не может быть = null") Long> cargoIds) {  // List<@NotNull Long> – (во 2-ю очередь) при наличии @Valid на параметре будет проверять, что каждый элемент списка не null

        CargoCalculationResponse response = cargoService.calculatePrice(cargoIds);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reservations")
    public ResponseEntity<CargoReservationResponse> reserveItems(
            @RequestBody
            @Valid
            @NotEmpty(message = "Список резервируемых товаров не может быть пустым")
            List<@NotNull (message = "Запрос по артикулу не может быть = null") CargoReservationRequest> requests) {
        CargoReservationResponse response = cargoService.reserveItems(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

//    @PostMapping("/reservations/confirm")
//    public ResponseEntity<Void> confirmReservation(
//            @RequestBody @Valid ConfirmReservationRequest request) {
//        cargoService.confirmReservation(request);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/reservations/cancel")
//    public ResponseEntity<Void> cancelReservation(
//            @RequestParam UUID orderId) {
//        cargoService.cancelReservation(orderId);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/reservations/{orderId}")
//    public ResponseEntity<CargoReservationResponse> getReservationByOrderId(
//            @PathVariable UUID orderId) {
//        CargoReservationResponse response = cargoService.getReservationByOrderId(orderId);
//        return ResponseEntity.ok(response);
//    }
}

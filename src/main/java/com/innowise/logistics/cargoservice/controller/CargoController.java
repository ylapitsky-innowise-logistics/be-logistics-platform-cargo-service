package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.CargoCalculationRequest;
import com.innowise.logistics.cargoservice.dto.request.CargoReservationRequest;
import com.innowise.logistics.cargoservice.dto.response.CargoCalculationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoReservationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoResponseDto;
import com.innowise.logistics.cargoservice.service.CargoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
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


    @GetMapping("/items")
    public ResponseEntity<Page<CargoResponseDto>> getCatalogItems(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Page<CargoResponseDto> items = cargoService.getCatalogItems(pageable);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<CargoResponseDto> getCatalogItemById(@PathVariable Long id) {
        CargoResponseDto item = cargoService.getCatalogItemById(id);
        return ResponseEntity.ok(item);
    }


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

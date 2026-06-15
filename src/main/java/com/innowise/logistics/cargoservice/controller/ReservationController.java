package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.CargoReservationRequest;
import com.innowise.logistics.cargoservice.dto.response.CargoReservationResponse;
import com.innowise.logistics.cargoservice.service.ReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;


    @PostMapping("")
    public ResponseEntity<CargoReservationResponse> reserveItems(
            @RequestBody
            @Valid
            @NotEmpty(message = "Список резервируемых товаров не может быть пустым")
            List<@NotNull(message = "Запрос по артикулу не может быть = null") CargoReservationRequest> requests) {
        CargoReservationResponse response = reservationService.reserveItems(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

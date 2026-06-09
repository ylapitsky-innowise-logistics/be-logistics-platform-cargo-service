package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.CargoCalculationRequest;
import com.innowise.logistics.cargoservice.dto.CargoCalculationResponse;
import com.innowise.logistics.cargoservice.dto.CargoResponseDto;
import com.innowise.logistics.cargoservice.service.CargoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
            @Valid @RequestBody List<CargoCalculationRequest> requests) {

        CargoCalculationResponse response = cargoService.calculatePrice(requests);
        return ResponseEntity.ok(response);
    }
}

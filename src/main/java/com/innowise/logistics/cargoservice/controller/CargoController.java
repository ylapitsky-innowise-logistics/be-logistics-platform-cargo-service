package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.CargoResponseDto;
import com.innowise.logistics.cargoservice.service.CargoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.AddressCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.AddressUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.AddressCreatingResponse;
import com.innowise.logistics.cargoservice.dto.response.AddressViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.innowise.logistics.cargoservice.service.AddressService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/test-data")
@RequiredArgsConstructor
public class TestDataController {

    private final AddressService addressService;

    /**
     * POST /api/v1/test-data/generate
     * Генерация и сохранение указанного количества фейковых адресов.
     */
    @PostMapping("/generate")
    public ResponseEntity<AddressCreatingResponse> createAddress(
            @RequestParam(name = "addresses", required = false, defaultValue = "10") int addressesQuantity,
            @RequestParam(name = "locations", required = false, defaultValue = "10") int locationsQuantity,
            @RequestParam(name = "dimensions", required = false, defaultValue = "10") int dimensionsQuantity,
            @RequestParam(name = "skus", required = false, defaultValue = "10") int skusQuantity,
            @RequestParam(name = "cargos", required = false, defaultValue = "20") int cargosQuantity
            ) {
        log.info("""
                Количество адресов: {}
                Количество локаций: {}
                Количество размеров товара: {}
                Количество артикулов: {}
                Количество товаров: {}
                """,
                addressesQuantity,
                locationsQuantity,
                dimensionsQuantity,
                skusQuantity,
                cargosQuantity);

        AddressCreatingResponse response = addressService.createAddress(request);
        return ResponseEntity.ok(response);
    }
}

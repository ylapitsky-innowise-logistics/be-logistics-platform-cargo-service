package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.AddressCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.AddressUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.AddressCreatingResponse;
import com.innowise.logistics.cargoservice.dto.response.AddressViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.innowise.logistics.cargoservice.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/catalog/addresses")
@RequiredArgsConstructor
@Validated
public class AddressController {

    private final AddressService addressService;

    /**
     * 1️⃣ POST /api/v1/catalog/addresses
     * C - Create: Создание нового адреса.
     */
    @PostMapping
    public ResponseEntity<AddressCreatingResponse> createAddress(
            @RequestBody
            @Valid
            AddressCreatingRequest request) {

        AddressCreatingResponse response = addressService.createAddress(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 2️⃣ GET /api/v1/catalog/addresses/{id}
     * R - Read: Получить выбранный адрес по его ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AddressViewResponse> getAddressById(
            @PathVariable
            Long id) {

        AddressViewResponse response = addressService.getAddressById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 3️⃣ GET /api/v1/catalog/addresses
     * R - Read: Просмотр списка всех адресов с пагинацией.
     * Паттерн полностью соответствует просмотру товаров.
     */
    @GetMapping
    public ResponseEntity<PageResponse<AddressViewResponse>> getAllAddresses(
            @PageableDefault(size = 20, sort = "id")
            Pageable pageable) {

        Page<AddressViewResponse> page = addressService.getAllAddresses(pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    /**
     * 4️⃣ PUT /api/v1/catalog/addresses/{id}
     * U - Update: Полное обновление существующего адреса по ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AddressViewResponse> updateAddress(
            @PathVariable Long id,
            @RequestBody
            @Valid
            AddressUpdateRequest request) {

        AddressViewResponse response = addressService.updateAddress(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 5️⃣ DELETE /api/v1/catalog/addresses/{id}
     * D - Delete: Удаление адреса из системы по его ID.
     * Согласно REST-конвенции, при успешном удалении без возвращаемого тела отдаем 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable
            Long id) {

        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}

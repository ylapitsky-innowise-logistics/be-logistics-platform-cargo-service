package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.DimensionCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.DimensionUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.DimensionCreatingResponse;
import com.innowise.logistics.cargoservice.dto.response.DimensionViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.innowise.logistics.cargoservice.service.DimensionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/catalog/dimensions")
@RequiredArgsConstructor
@Validated
public class DimensionController {

    private final DimensionService dimensionService;

    @PostMapping
    public ResponseEntity<DimensionCreatingResponse> createDimension(
            @RequestBody @Valid DimensionCreatingRequest request) {
        return ResponseEntity.ok(dimensionService.createDimension(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DimensionViewResponse> getDimensionById(@PathVariable Long id) {
        return ResponseEntity.ok(dimensionService.getDimensionById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<DimensionViewResponse>> getAllDimensions(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<DimensionViewResponse> page = dimensionService.getAllDimensions(pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DimensionViewResponse> updateDimension(
            @PathVariable Long id,
            @RequestBody @Valid DimensionUpdateRequest request) {
        return ResponseEntity.ok(dimensionService.updateDimension(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDimension(@PathVariable Long id) {
        dimensionService.deleteDimension(id);
        return ResponseEntity.noContent().build();
    }
}

package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.dto.request.LocationCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.LocationUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.LocationCreatingResponse;
import com.innowise.logistics.cargoservice.dto.response.LocationViewResponse;
import com.innowise.logistics.cargoservice.dto.response.PageResponse;
import com.innowise.logistics.cargoservice.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/catalog/locations")
@RequiredArgsConstructor
@Validated
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationCreatingResponse> createLocation(
            @RequestBody @Valid LocationCreatingRequest request) {
        return ResponseEntity.ok(locationService.createLocation(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationViewResponse> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<LocationViewResponse>> getAllLocations(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<LocationViewResponse> page = locationService.getAllLocations(pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationViewResponse> updateLocation(
            @PathVariable Long id,
            @RequestBody @Valid LocationUpdateRequest request) {
        return ResponseEntity.ok(locationService.updateLocation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}

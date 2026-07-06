package com.innowise.logistics.cargoservice.service;

import com.innowise.logistics.cargoservice.dto.request.LocationCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.LocationUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.LocationCreatingResponse;
import com.innowise.logistics.cargoservice.dto.response.LocationViewResponse;
import com.innowise.logistics.cargoservice.entity.Address;
import com.innowise.logistics.cargoservice.entity.Location;
import com.innowise.logistics.cargoservice.repository.AddressRepository;
import com.innowise.logistics.cargoservice.repository.LocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;
    private final AddressRepository addressRepository; // 🎯 Внедряем для проверки целостности связей

    @Transactional
    public LocationCreatingResponse createLocation(LocationCreatingRequest request) {
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException("Адрес склада с ID " + request.getAddressId() + " не найден"));

        Location location = new Location();
        location.setRack(request.getRack());
        location.setShelf(request.getShelf());
        location.setAddress(address); // Привязываем managed-сущность адреса

        Location saved = locationRepository.save(location);
        return new LocationCreatingResponse(saved.getId());
    }

    public LocationViewResponse getLocationById(Long id) {
        return locationRepository.findById(id)
                .map(this::mapToViewResponse)
                .orElseThrow(() -> new EntityNotFoundException("Ячейка хранения с ID " + id + " не найдена"));
    }

    public Page<LocationViewResponse> getAllLocations(Pageable pageable) {
        return locationRepository.findAll(pageable)
                .map(this::mapToViewResponse);
    }

    @Transactional
    public LocationViewResponse updateLocation(Long id, LocationUpdateRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ячейка хранения с ID " + id + " не найдена"));

        // Если адрес в запросе изменился — находим новый и перепривязываем его
        if (!location.getAddress().getId().equals(request.getAddressId())) {
            Address newAddress = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new EntityNotFoundException("Адрес склада с ID " + request.getAddressId() + " не найден"));
            location.setAddress(newAddress);
        }

        location.setRack(request.getRack());
        location.setShelf(request.getShelf());

        return mapToViewResponse(location);
    }

    @Transactional
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new EntityNotFoundException("Ячейка хранения с ID " + id + " не найдена");
        }
        locationRepository.deleteById(id);
    }

    private LocationViewResponse mapToViewResponse(Location location) {
        return new LocationViewResponse(
                location.getId(),
                location.getRack(),
                location.getShelf(),
                location.getAddress().getId() // Безопасно для LAZY: не генерирует лишний SELECT к таблице адресов
        );
    }
}

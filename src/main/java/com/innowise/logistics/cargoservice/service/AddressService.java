package com.innowise.logistics.cargoservice.service;

import com.innowise.logistics.cargoservice.dto.request.AddressCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.AddressUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.AddressCreatingResponse;
import com.innowise.logistics.cargoservice.dto.response.AddressViewResponse;
import com.innowise.logistics.cargoservice.entity.Address;
import com.innowise.logistics.cargoservice.repository.AddressRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Оптимизация для операций чтения
public class AddressService {

    private final AddressRepository addressRepository;

    /**
     * C - Create: Создание адреса
     */
    @Transactional
    public AddressCreatingResponse createAddress(AddressCreatingRequest request) {
        // 1. Проверяем, существует ли уже такой адрес
        Optional<Address> existingAddress = addressRepository
                .findByCountryAndZipCodeAndCityAndMicrodistrictAndStreetAndHouseAndBlockAndApartment(
                        request.getCountry(),
                        request.getZipCode(),
                        request.getCity(),
                        request.getMicrodistrict(),
                        request.getStreet(),
                        request.getHouse(),
                        request.getBlock(),
                        request.getApartment()
                );

        // 2. Если существует — возвращаем его ID
        if (existingAddress.isPresent()) {
            return new AddressCreatingResponse(existingAddress.get().getId());
        }

        // 3. Если нет — создаём новый
        Address address = new Address();
        mapCreatingRequestToEntity(request, address);
        Address savedAddress = addressRepository.save(address);
        return new AddressCreatingResponse(savedAddress.getId());
    }

    /**
     * R - Read: Получение одного адреса по ID
     */
    public AddressViewResponse getAddressById(Long id) {
        return addressRepository.findById(id)
                .map(this::mapToViewResponse)
                .orElseThrow(() -> new EntityNotFoundException("Адрес с ID " + id + " не найден"));
    }

    /**
     * R - Read: Получение страницы (Page) адресов
     */
    public Page<AddressViewResponse> getAllAddresses(Pageable pageable) {
        return addressRepository.findAll(pageable)
                .map(this::mapToViewResponse);
    }

    /**
     * U - Update: Полное обновление адреса
     */
    @Transactional
    public AddressViewResponse updateAddress(Long id, AddressUpdateRequest request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Адрес с ID " + id + " не найден"));

        // Обновляем состояние сущности (благодаря managed-статусу изменения улетят в базу при коммите)
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setCity(request.getCity());
        address.setMicrodistrict(request.getMicrodistrict());
        address.setStreet(request.getStreet());
        address.setHouse(request.getHouse());
        address.setBlock(request.getBlock());
        address.setApartment(request.getApartment());

        return mapToViewResponse(address);
    }

    /**
     * D - Delete: Удаление адреса по ID
     */
    @Transactional
    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new EntityNotFoundException("Адрес с ID " + id + " не найден");
        }
        addressRepository.deleteById(id);
    }

    // =========================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ МАППИНГА (В будущем можно заменить на MapStruct)
    // =========================================================

    private void mapCreatingRequestToEntity(AddressCreatingRequest request, Address address) {
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setCity(request.getCity());
        address.setMicrodistrict(request.getMicrodistrict());
        address.setStreet(request.getStreet());
        address.setHouse(request.getHouse());
        address.setBlock(request.getBlock());
        address.setApartment(request.getApartment());
    }

    private AddressViewResponse mapToViewResponse(Address address) {
        return new AddressViewResponse(
                address.getId(),
                address.getCountry(),
                address.getZipCode(),
                address.getCity(),
                address.getMicrodistrict(),
                address.getStreet(),
                address.getHouse(),
                address.getBlock(),
                address.getApartment()
        );
    }
}

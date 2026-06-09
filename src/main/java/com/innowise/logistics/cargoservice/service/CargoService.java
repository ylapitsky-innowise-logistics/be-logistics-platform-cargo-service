package com.innowise.logistics.cargoservice.service;

import com.innowise.logistics.cargoservice.dto.CargoCalculationRequest;
import com.innowise.logistics.cargoservice.dto.CargoCalculationResponse;
import com.innowise.logistics.cargoservice.dto.CargoResponseDto;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Status;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CargoService {

    private final CargoRepository cargoRepository;

    @Transactional(readOnly = true)
    public Page<CargoResponseDto> getCatalogItems(Pageable pageable) {
        Page<Cargo> cargoPage = cargoRepository.findAll(pageable);

        // Маппим Entity в DTO "на лету"
        return cargoPage.map(this::convertToDto);
    }

    private CargoResponseDto convertToDto(Cargo cargo) {
        String dimensionsStr = String.format("%.1fx%.1fx%.1f",
                cargo.getDimension().getLength(),
                cargo.getDimension().getWidth(),
                cargo.getDimension().getHeight());

        String locationStr = String.format("%s / %s",
                cargo.getLocation().getRack(),
                cargo.getLocation().getShelf() != null ? cargo.getLocation().getShelf() : "Нет полки");

        return new CargoResponseDto(
                cargo.getId(),
                cargo.getSku().getName(),
                cargo.getMongoDocId(),
                cargo.getName(),
                cargo.getCategory(),
                cargo.getWeight(),
                dimensionsStr,
                cargo.getPrice(),
                locationStr,
                cargo.getCreatedAt(),
                cargo.getStatus(),
                cargo.getStatusAt()
        );
    }


    @Transactional(readOnly = true)
    public CargoResponseDto getCatalogItemById(Long id) {
        return cargoRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Товар с id = %d не найден в каталоге", id)
                ));
    }


    @Transactional(readOnly = true)
    public CargoCalculationResponse calculatePrice(List<CargoCalculationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new CargoCalculationResponse(BigDecimal.ZERO, 0.0, 0, "RUB");
        }

        // 1. Извлекаем уникальные ID, чтобы сделать ОДИН пачкой запрос к БД
        Set<Long> cargoIds = requests.stream()
                .map(CargoCalculationRequest::cargoId)
                .collect(Collectors.toSet());

        // 2. Достаем сущности из базы и превращаем в Map для быстрого доступа O(1)
        Map<Long, Cargo> cargoMap = cargoRepository.findAllById(cargoIds).stream()
                .collect(Collectors.toMap(Cargo::getId, cargo -> cargo));

        BigDecimal totalPrice = BigDecimal.ZERO;
        Double totalWeight = 0.0;
        int totalItemsCount = 0;

        // 3. Бежим по запросу пользователя и калькулируем в памяти
        for (CargoCalculationRequest req : requests) {
            Cargo cargo = cargoMap.get(req.cargoId());

            // Валидация: существует ли товар вообще?
            if (cargo == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Товар с id = %d не существует в каталоге", req.cargoId())
                );
            }

            // Валидация: доступен ли он? (Если он RESERVED или SHIPPED, его нельзя считать в корзину)
            if (cargo.getStatus() != Status.AVAILABLE) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Товар с id = %d сейчас недоступен для покупки (Статус: %s)",
                                req.cargoId(), cargo.getStatus())
                );
            }

            BigDecimal quantityMultiplier = BigDecimal.valueOf(req.quantity());

            // Считаем деньги: Цена * Количество
            BigDecimal itemSum = cargo.getPrice().multiply(quantityMultiplier);
            totalPrice = totalPrice.add(itemSum);

            // Считаем вес: Вес * Количество
            totalWeight += (cargo.getWeight() * req.quantity());

            // Считаем общее кол-во штук
            totalItemsCount += req.quantity();
        }

        return new CargoCalculationResponse(totalPrice, totalWeight, totalItemsCount, "RUB");
    }
}

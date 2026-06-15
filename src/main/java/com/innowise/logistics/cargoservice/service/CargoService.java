package com.innowise.logistics.cargoservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.logistics.cargoservice.dto.response.CargoCalculationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoViewResponse;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Status;
import com.innowise.logistics.cargoservice.mapper.CargoMapper;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import com.innowise.logistics.cargoservice.repository.ReservationRepository;
import com.innowise.logistics.cargoservice.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.innowise.logistics.cargoservice.constant.Constants.CYRRENCY;

@Slf4j
@Service
@RequiredArgsConstructor
public class CargoService {

    private final CargoRepository cargoRepository;
    private final ReservationRepository reservationRepository;
    private final SkuRepository skuRepository;
    private final CargoMapper cargoMapper;
    private final ObjectMapper objectMapper;

    /**
     * 1️⃣ Просмотр агрегированного списка всех доступных товаров.
     */
    @Transactional(readOnly = true)
    public Page<CargoViewResponse> getAllCargos(Pageable pageable) {
        Page<Cargo> cargoPage = cargoRepository.findAll(pageable);

        // Маппим Entity в DTO "на лету"
        return cargoPage.map(cargoMapper::toDto);
    }

    /**
     * 2️⃣ Получить выбранный товар по его ID.
     * @param id выбранный товар (его id)
     * @return DTO выбранного товара
     */
    @Transactional(readOnly = true)
    public CargoViewResponse getCargoById(Long id) {
        return cargoRepository.findById(id)
                .map(cargoMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Товар с id = %d не найден в каталоге", id)
                ));
    }

    /**
     * 3️⃣ Рассчет стоимости списка товаров по переданному списку их 'id'
     * @param cargoIds список id товаров
     * @return стоимость, зашитая в объекте CargoCalculationResponse (+немного контекста)
     */
    @Transactional(readOnly = true)
    public CargoCalculationResponse calculatePriceByIds(List<Long> cargoIds) {
        log.debug("Попытка рассчета стоимости {} товаров из списка: {}", cargoIds.size(), Arrays.toString(cargoIds.toArray()));
        if (cargoIds.isEmpty()) {
            log.error("Список товаров пуст");
            return new CargoCalculationResponse(BigDecimal.ZERO, 0.0, 0, CYRRENCY);
        }

        // 1. Достаем сущности из базы и превращаем в Map для быстрого доступа O(1)
        Map<Long, Cargo> cargoMap = cargoRepository.findAllById(cargoIds).stream()
                .collect(Collectors.toMap(Cargo::getId, cargo -> cargo));

        BigDecimal totalPrice = BigDecimal.ZERO;
        Double totalWeight = 0.0;

        // 2. Бежим по запросу пользователя и калькулируем в памяти
        for (Long id : cargoIds) {
            Cargo cargo = cargoMap.get(id);

            // Валидация: существует ли товар вообще?
            if (cargo == null) {
                log.warn("Товар с id = {} не существует в каталоге", id);  // предупреждение, т.к. ожидаемое явление
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Товар с id = %d не существует в каталоге", id)
                );
            }

            // Валидация: доступен ли он? (Если его статус != AVAILABLE, его нельзя считать в корзину)
            if (cargo.getStatus() != Status.AVAILABLE) {
                log.warn("Товар с id = {} сейчас недоступен для покупки (Статус: {})", id, cargo.getStatus());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Товар с id = %d сейчас недоступен для покупки", id)
                );
            }

            totalPrice = totalPrice.add(cargo.getPrice());
            totalWeight += cargo.getWeight();
        }

        return new CargoCalculationResponse(totalPrice, totalWeight, cargoIds.size(), CYRRENCY);
    }
}

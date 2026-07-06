package com.innowise.logistics.cargoservice.service;

import com.innowise.logistics.cargoservice.dto.request.CargoCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.CargoUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.CargoCalculationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoCreatingResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoViewResponse;
import com.innowise.logistics.cargoservice.entity.*;
import com.innowise.logistics.cargoservice.mapper.CargoMapper;
import com.innowise.logistics.cargoservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.innowise.logistics.cargoservice.constant.Constants.CURRENCY;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 🟢 Глобальная оптимизация под read-only
public class CargoService {

    private final CargoRepository cargoRepository;
//    private final ReservationRepository reservationRepository;
    private final SkuRepository skuRepository;
    private final DimensionRepository dimensionRepository; // 🎯 Внедряем для валидации связей
    private final LocationRepository locationRepository;   // 🎯 Внедряем для валидации связей
    private final CargoMapper cargoMapper;
//    private final ObjectMapper objectMapper;

    // =========================================================
    // СТАНДАРТНЫЕ CRUD ОПЕРАЦИИ ДЛЯ CARGO
    // =========================================================

    /**
     * C - Create: Поступление новой единицы груза на склад
     */
    @Transactional
    public CargoCreatingResponse createCargo(CargoCreatingRequest request) {
        log.info("Регистрация поступления нового груза: {}", request.getName());

        Sku sku = skuRepository.findById(request.getSkuId())
                .orElseThrow(() -> new EntityNotFoundException("Артикул (SKU) с ID " + request.getSkuId() + " не найден"));
        Dimension dimension = dimensionRepository.findById(request.getDimensionId())
                .orElseThrow(() -> new EntityNotFoundException("Габариты с ID " + request.getDimensionId() + " не найдены"));
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Ячейка склада с ID " + request.getLocationId() + " не найдена"));

        Cargo cargo = new Cargo();
        cargo.setSku(sku);
        cargo.setName(request.getName());
        cargo.setCategory(request.getCategory());
        cargo.setWeight(request.getWeight());
        cargo.setDimension(dimension);
        cargo.setPrice(request.getPrice());
        cargo.setLocation(location);
        // Поля createdAt, status и statusAt инициализируются автоматически на уровне сущности/аудита

        Cargo savedCargo = cargoRepository.save(cargo);
        return new CargoCreatingResponse(savedCargo.getId());
    }


    /**
     * U - Update: Полное изменение параметров существующего груза
     */
    @Transactional
    public CargoViewResponse updateCargo(Long id, CargoUpdateRequest request) {
        log.info("Обновление данных груза с ID: {}", id);

        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Товар с id = " + id + " не найден в каталоге"));

        // Если связи изменились — подтягиваем новые и проверяем их валидность
        if (!cargo.getSku().getId().equals(request.getSkuId())) {
            Sku newSku = skuRepository.findById(request.getSkuId())
                    .orElseThrow(() -> new EntityNotFoundException("Артикул (SKU) с ID " + request.getSkuId() + " не найден"));
            cargo.setSku(newSku);
        }
        if (!cargo.getDimension().getId().equals(request.getDimensionId())) {
            Dimension newDimension = dimensionRepository.findById(request.getDimensionId())
                    .orElseThrow(() -> new EntityNotFoundException("Габариты с ID " + request.getDimensionId() + " не найдены"));
            cargo.setDimension(newDimension);
        }
        if (!cargo.getLocation().getId().equals(request.getLocationId())) {
            Location newLocation = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Ячейка склада с ID " + request.getLocationId() + " не найдена"));
            cargo.setLocation(newLocation);
        }

        cargo.setName(request.getName());
        cargo.setCategory(request.getCategory());
        cargo.setWeight(request.getWeight());
        cargo.setPrice(request.getPrice());

        return cargoMapper.toDto(cargo);
    }

    /**
     * D - Delete: Удаление единицы груза из каталога по ID
     */
    @Transactional
    public void deleteCargo(Long id) {
        log.warn("Удаление единицы груза с ID: {}", id);
        if (!cargoRepository.existsById(id)) {
            throw new EntityNotFoundException("Товар с id = " + id + " не найден в каталоге");
        }
        cargoRepository.deleteById(id);
    }

    // =========================================================
    // 📊 БЛОК ОРИГИНАЛЬНОЙ БИЗНЕС-ЛОГИКИ (СОХРАНЕН НА 100%)
    // =========================================================

    /**
     * 1️⃣ Просмотр агрегированного списка всех доступных товаров.
     */
    public Page<CargoViewResponse> getAllCargos(Pageable pageable) {
        Page<Cargo> cargoPage = cargoRepository.findAll(pageable);
        return cargoPage.map(cargoMapper::toDto);
    }

    /**
     * 2️⃣ Получить выбранный товар по его ID.
     */
    public CargoViewResponse getCargoById(Long id) {
        return cargoRepository.findById(id)
                .map(cargoMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Товар с id = %d не найден в каталоге", id)
                ));
    }

    /**
     * 3️⃣ Рассчет стоимости списка товаров по переданному списку их 'id'
     */
    public CargoCalculationResponse calculatePriceByIds(List<Long> cargoIds) {
        log.debug("Попытка рассчета стоимости {} товаров из списка: {}", cargoIds.size(), Arrays.toString(cargoIds.toArray()));
        if (cargoIds.isEmpty()) {
            log.error("Список товаров пуст");
            return new CargoCalculationResponse(BigDecimal.ZERO, 0.0, 0, CURRENCY);
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
                log.warn("Товар с id = {} не существует в каталоге", id);
                throw new IllegalArgumentException(
                        String.format("Товар с id = %d не существует в каталоге", id)
                );
            }

            // Валидация: доступен ли он? (Если его статус != AVAILABLE, его нельзя считать в корзину)
            if (cargo.getStatus() != Status.AVAILABLE) {
                log.warn("Товар с id = {} сейчас недоступен для покупки (Статус: {})", id, cargo.getStatus());
                throw new IllegalStateException(
                        String.format("Товар с id = %d сейчас недоступен для покупки", id)
                );
            }

            totalPrice = totalPrice.add(cargo.getPrice());
            totalWeight += cargo.getWeight();
        }

        return new CargoCalculationResponse(totalPrice, totalWeight, cargoIds.size(), CURRENCY);
    }
}

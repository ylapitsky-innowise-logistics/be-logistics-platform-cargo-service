package com.innowise.logistics.cargoservice.service;

import com.innowise.logistics.cargoservice.dto.request.CargoReservationRequest;
import com.innowise.logistics.cargoservice.dto.response.CargoCalculationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoReservationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoViewResponse;
import com.innowise.logistics.cargoservice.dto.response.SkuAvailabilityResponse;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Reservation;
import com.innowise.logistics.cargoservice.entity.Sku;
import com.innowise.logistics.cargoservice.entity.Status;
import com.innowise.logistics.cargoservice.mapper.CargoMapper;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import com.innowise.logistics.cargoservice.repository.ReservationRepository;
import com.innowise.logistics.cargoservice.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
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

    @Transactional(readOnly = true)
    public Page<CargoViewResponse> getCatalogItems(Pageable pageable) {
        Page<Cargo> cargoPage = cargoRepository.findAll(pageable);

        // Маппим Entity в DTO "на лету"
        return cargoPage.map(cargoMapper::toDto);
    }

    @Transactional(readOnly = true)
    public CargoViewResponse getCatalogItemById(Long id) {
        return cargoRepository.findById(id)
                .map(cargoMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Товар с id = %d не найден в каталоге", id)
                ));
    }


    // прилетел список id товаров, выплевываем стоимость, зашитую в объекте CargoCalculationResponse (+немного контекста)
    @Transactional(readOnly = true)
    public CargoCalculationResponse calculatePrice(List<Long> cargoIds) {
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

    // каталог уникальных товаров
    @Transactional(readOnly = true)
    public Page<SkuAvailabilityResponse> getAvailableSkus(Pageable pageable) {
        return cargoRepository.findAvailableSkuStats(
                Status.AVAILABLE,
                pageable
        );
    }


//    @Transactional(readOnly = true)
//    public List<SkuItemsResponse> getAvailableItemsBySku(Long skuId) {
//        return cargoRepository.findBySkuIdAndStatus(skuId, Status.AVAILABLE)
//                .stream()
//                .map(cargo -> new SkuItemsResponse(
//                        cargo.getId(),
//                        cargo.getName(),
//                        cargo.getWeight(),
//                        cargo.getPrice(),
//                        formatLocation(cargo.getLocation()),
//                        cargo.getCreatedAt()
//                ))
//                .toList();
//    }

    /// ///////////////////////////////////////////////////////////////////////////////////
    // Резервирование


    @Transactional
    public CargoReservationResponse reserveItems(List<CargoReservationRequest> requests) {
        log.info("Попытка резервирования списка товаров из {} шт.", requests.size());

        if (requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Список артикулов товаров пуст");
        }

        // 1. Делаем выборку по каждому артикулу. Если товаров не хватает - выдаем ошибку и сообщаем сколько на данный момент свободно товаров по каждому из переданных артикулу
        Set<Cargo> reservationCargos = new HashSet<>();                       // Список найденных по запросу товаров, подлежащих резервированию
        for (CargoReservationRequest request : requests) {
            if (request.getQuantity() <= 0) {
                log.error("Для артикула с id = {} количество = {}. Количество должно быть > 0",
                        request.getSkuId(), request.getQuantity());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Количество должно быть > 0"
                );
            }

            Optional<Sku> sku = skuRepository.findById(request.getSkuId());
            if (sku.isEmpty()) {
                log.error("Артикула с id = {} не существует", request.getSkuId());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Передан не верный артикул товара."
                );
            }

            List<Cargo> availableCargosBySkus = cargoRepository.findFirstNAvailableBySkuIdAndStatus(
                    request.getSkuId(),
                    Status.AVAILABLE,
                    PageRequest.of(0, request.getQuantity()));

            if (!request.getQuantity().equals(availableCargosBySkus.size())) {
                log.error("Недостаточное количество товара с артикулом {} на складе. Нужно {} а аеть в наличии только {}",
                        sku.get(),
                        request.getQuantity(),
                        availableCargosBySkus.size());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Недостаточное количество товара на складе."
                );
            }
            reservationCargos.addAll(availableCargosBySkus);
        }

        // 2. Если все хорошо - Бронируем выбранные товары и возвращаем ссылку на их бронь
        Set<Long> reservationCargoIds = new HashSet<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        Double totalWeight = 0.0;

        for (Cargo cargo: reservationCargos) {
            totalPrice = totalPrice.add(cargo.getPrice());
            totalWeight += cargo.getWeight();
            reservationCargoIds.add(cargo.getId());
            cargo.updateStatus(Status.RESERVED);
        }
        cargoRepository.saveAll(reservationCargos);

        Reservation reservation = new Reservation();
        reservation.setCargoIds(reservationCargoIds);
        reservation.setIsActive(true);
        reservation.setTotalPrice(totalPrice);
        reservation.setTotalWeight(totalWeight);
        reservation.setTotalQuantity(reservationCargos.size());
        reservation.setCurrency(CYRRENCY);
        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Закончено резервирование {} товаров. id={}", reservation.getCargoIds().size(), reservation.getId());

        return new CargoReservationResponse(
                savedReservation.getId(),
                savedReservation.getCreatedAt(),
                savedReservation.getTotalPrice(),
                savedReservation.getTotalWeight(),
                savedReservation.getTotalQuantity(),
                CYRRENCY);

        // 3. возможность разбронирования товаров
        // 4. возможность отгрузки товаров в доставку
    }
}

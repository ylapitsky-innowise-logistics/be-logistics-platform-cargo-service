package com.innowise.logistics.cargoservice.service;

import com.innowise.logistics.cargoservice.dto.request.CargoCalculationRequest;
import com.innowise.logistics.cargoservice.dto.request.CargoReservationRequest;
import com.innowise.logistics.cargoservice.dto.response.CargoCalculationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoReservationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoResponseDto;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Reservation;
import com.innowise.logistics.cargoservice.entity.Sku;
import com.innowise.logistics.cargoservice.entity.Status;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import com.innowise.logistics.cargoservice.repository.ReservationRepository;
import com.innowise.logistics.cargoservice.repository.SkuRepository;
import jakarta.validation.Valid;
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
    public CargoCalculationResponse calculatePrice(List<Long> requests) {
        log.debug("Попытка рассчета стоимости {} товаров из списка: {}", requests.size(), Arrays.toString(requests.toArray()));
        if (requests == null || requests.isEmpty()) {
            log.error("Список товаров пуст или == null");
            return new CargoCalculationResponse(BigDecimal.ZERO, 0.0, 0, CYRRENCY);
        }

        // 1. Извлекаем уникальные ID, чтобы сделать ОДИН пачкой запрос к БД
        Set<Long> cargoIds = new HashSet<>(requests);

        // 2. Достаем сущности из базы и превращаем в Map для быстрого доступа O(1)
        Map<Long, Cargo> cargoMap = cargoRepository.findAllById(cargoIds).stream()
                .collect(Collectors.toMap(Cargo::getId, cargo -> cargo));

        BigDecimal totalPrice = BigDecimal.ZERO;
        Double totalWeight = 0.0;
        int totalItemsCount = 0;

        // 3. Бежим по запросу пользователя и калькулируем в памяти
        for (Long req : requests) {
            Cargo cargo = cargoMap.get(req);

            // Валидация: существует ли товар вообще?
            if (cargo == null) {
                log.error("Товар с id = {} не существует в каталоге", req);
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Товар с id = %d не существует в каталоге", req)
                );
            }

            // Валидация: доступен ли он? (Если его статус != AVAILABLE, его нельзя считать в корзину)
            if (cargo.getStatus() != Status.AVAILABLE) {
                log.error("Товар с id = {} сейчас недоступен для покупки (Статус: {})", req, cargo.getStatus());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Товар с id = %d сейчас недоступен для покупки", req)
                );
            }

            totalPrice = totalPrice.add(cargo.getPrice());
            totalWeight += cargo.getWeight();
            totalItemsCount += req;
        }

        return new CargoCalculationResponse(totalPrice, totalWeight, totalItemsCount, CYRRENCY);
    }



    /// ///////////////////////////////////////////////////////////////////////////////////
    // Резервирование


    @Transactional
    public CargoReservationResponse reserveItems(List<CargoReservationRequest> requests) {
        log.info("Попытка резервирования списка {} (кол-во) товаров.", requests.size());

        // 1. Делаем выборку по каждому артикулу. Если товаров не хватает - выдаем ошибку и сообщаем сколько на данный момент свободно товаров по каждому из переданных артикулу
        // 2. Если все хорошо - Бронируем выбранные товары и возвращаем ссылку на их бронь
        // 3. возможность разбронирования товаров
        // 4. возможность отгрузки товаров в доставку


        Set<Cargo> existCargos = new HashSet<>(); // Список ВСЕХ Валидных товары из базы, подлежащих резервированию
        for (CargoReservationRequest request : requests) {
            Optional<Sku> sku = skuRepository.findById(request.skuId());
            if (sku.isEmpty()) {
                log.error("Артикула с id = {} не существует", request.skuId());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Передан не верный артикул товара."
                );
            }

            List<Cargo> existCargosBySkus = cargoRepository.findFirstNAvailableBySkuIdAndStatus(
                    request.skuId(),
                    Status.AVAILABLE,
                    PageRequest.of(0, request.quantity()));

            if (!request.quantity().equals(existCargosBySkus.size())) {
                log.error("Недостаточное количество товара с артикулом {} на складе. Нужно {} а аеть в наличии только {}",
                        sku.get(),
                        request.quantity(),
                        existCargosBySkus.size());
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Недостаточное количество товара на складе."
                );
            }
            existCargos.addAll(existCargosBySkus);
        }

        // резервируем
        Set<Long> existCargoIds = new HashSet<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        Double totalWeight = 0.0;

        for (Cargo cargo: existCargos) {
            totalPrice = totalPrice.add(cargo.getPrice());
            totalWeight += cargo.getWeight();
            existCargoIds.add(cargo.getId());
            cargo.updateStatus(Status.RESERVED);
            cargoRepository.save(cargo);
        }

        Reservation reservation = new Reservation();
        reservation.setCargoIds(existCargoIds);
        reservation.setIsActive(true);
        reservation.setTotalPrice(totalPrice);
        reservation.setTotalWeight(totalWeight);
        reservation.setTotalQuantity(existCargos.size());
        reservation.setCurrency(CYRRENCY);
        Reservation savedReservation = reservationRepository.save(reservation);

        return new CargoReservationResponse(
                savedReservation.getId(),
                savedReservation.getCreatedAt(),
                savedReservation.getTotalPrice(),
                savedReservation.getTotalWeight(),
                savedReservation.getTotalQuantity(),
                CYRRENCY);
    }
}

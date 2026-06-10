package com.innowise.logistics.cargoservice.service;

import com.innowise.logistics.cargoservice.dto.request.CargoCalculationRequest;
import com.innowise.logistics.cargoservice.dto.request.CargoReservationRequest;
import com.innowise.logistics.cargoservice.dto.response.CargoCalculationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoReservationResponse;
import com.innowise.logistics.cargoservice.dto.response.CargoResponseDto;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Reservation;
import com.innowise.logistics.cargoservice.entity.Status;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

/*
    @Transactional
    public CargoReservationResponse reserveItems(List<CargoReservationRequest> requests) {
        log.info("Попытка резервирования списка {} (кол-во) товаров.", requests.size());

        // 1. Делаем выборку по каждому артикулу. Если товаров не хватает - выдаем ошибку и сообщаем сколько на данный момент свободно товаров по каждому из переданных артикулу
        // 2. Если все хорошо - Бронируем выбранные товары и возвращаем ссылку на их бронь
        // 3. возможность разбронирования товаров
        // 4. возможность отгрузки товаров в доставку

        // 1. Проверяем, нет ли уже активной брони для этого orderId
        Optional<Reservation> existingReservation = reservationRepository.findByOrderId(request.getOrderId());
        if (existingReservation.isPresent()) {
            Reservation existing = existingReservation.get();
            if (existing.getReservationStatus() == ReservationStatus.PENDING) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        String.format("Активная бронь для заказа %s уже существует", request.getOrderId())
                );
            }
        }

        // 2. Получаем все товары из БД одним запросом
        Set<Long> cargoIds = request.getItems().stream()
                .map(CargoReservationRequest.ReservationItem::getId)
                .collect(Collectors.toSet());

        Map<Long, Cargo> cargoMap = cargoRepository.findAllById(cargoIds).stream()
                .collect(Collectors.toMap(Cargo::getId, cargo -> cargo));

        // 3. Валидация и расчёт
        BigDecimal totalPrice = BigDecimal.ZERO;
        Double totalWeight = 0.0;
        Integer totalQuantity = 0;
        List<ReservationItem> reservationItems = new ArrayList<>();

        for (CargoReservationRequest.ReservationItem item : request.getItems()) {
            Cargo cargo = cargoMap.get(item.getId());

            if (cargo == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Товар с id = %d не существует", item.getId())
                );
            }

            if (cargo.getStatus() != Status.AVAILABLE) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Товар с id = %d недоступен для резервирования (статус: %s)",
                                item.getId(), cargo.getStatus())
                );
            }

            // Проверка достаточности количества (если нужно, добавьте поле availableQuantity в Cargo)
            // if (cargo.getAvailableQuantity() < item.getQuantity()) { ... }

            BigDecimal itemTotalPrice = cargo.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(itemTotalPrice);
            totalWeight += cargo.getWeight() * item.getQuantity();
            totalQuantity += item.getQuantity();

            // Создаём объект ReservationItem (без сохранения пока)
            ReservationItem reservationItem = ReservationItem.builder()
                    .cargo(cargo)
                    .requestedQuantity(item.getQuantity())
                    .reservedQuantity(item.getQuantity()) // TODO: если частичное резервирование
                    .pricePerUnit(cargo.getPrice())
                    .totalPrice(itemTotalPrice)
                    .build();
            reservationItems.add(reservationItem);
        }

        // 4. Создаём и сохраняем Reservation
        Reservation reservation = Reservation.builder()
                .orderId(request.getOrderId())
                .reservationStatus(ReservationStatus.PENDING)
                .expiresAt(request.getExpiresAt())
                .createdAt(Instant.now())
                .totalPrice(totalPrice)
                .totalWeight(totalWeight)
                .totalQuantity(totalQuantity)
                .currency(CYRRENCY)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 5. Привязываем reservation к каждому item и сохраняем
        for (ReservationItem item : reservationItems) {
            item.setReservation(savedReservation);
        }
        reservationItemRepository.saveAll(reservationItems);

        // 6. Меняем статус товаров на RESERVED
        for (Cargo cargo : cargoMap.values()) {
            cargo.setStatus(Status.RESERVED);
            cargo.setStatusAt(Instant.now());
        }
        cargoRepository.saveAll(cargoMap.values());

        // 7. Формируем ответ
        List<CargoReservationResponse.ReservedItem> responseItems = reservationItems.stream()
                .map(item -> new CargoReservationResponse.ReservedItem(
                        item.getCargo().getId(),
                        item.getCargo().getName(),
                        item.getRequestedQuantity(),
                        item.getReservedQuantity(),
                        item.getPricePerUnit(),
                        item.getTotalPrice()
                ))
                .toList();

        return new CargoReservationResponse(
                savedReservation.getOrderId(),
                savedReservation.getId(),
                savedReservation.getCreatedAt(),
                savedReservation.getExpiresAt(),
                ReservationStatus.PENDING,
                responseItems,
                savedReservation.getTotalPrice(),
                savedReservation.getTotalWeight(),
                savedReservation.getTotalQuantity(),
                savedReservation.getCurrency()
        );
    }
    */



}

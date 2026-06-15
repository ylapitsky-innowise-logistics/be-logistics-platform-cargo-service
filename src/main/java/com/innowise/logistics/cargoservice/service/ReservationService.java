package com.innowise.logistics.cargoservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.logistics.cargoservice.dto.request.CargoReservationRequest;
import com.innowise.logistics.cargoservice.dto.response.CargoReservationResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.innowise.logistics.cargoservice.constant.Constants.CYRRENCY;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final CargoRepository cargoRepository;
    private final ReservationRepository reservationRepository;
    private final SkuRepository skuRepository;
    private final CargoMapper cargoMapper;
    private final ObjectMapper objectMapper;

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


//    @PostMapping("/reservations/confirm")
//    public ResponseEntity<Void> confirmReservation(
//            @RequestBody @Valid ConfirmReservationRequest request) {
//        cargoService.confirmReservation(request);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/reservations/cancel")
//    public ResponseEntity<Void> cancelReservation(
//            @RequestParam UUID orderId) {
//        cargoService.cancelReservation(orderId);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/reservations/{orderId}")
//    public ResponseEntity<CargoReservationResponse> getReservationByOrderId(
//            @PathVariable UUID orderId) {
//        CargoReservationResponse response = cargoService.getReservationByOrderId(orderId);
//        return ResponseEntity.ok(response);
//    }
}

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    /**
     * 1️⃣ Зарезервировать товары
     * согласно переданному списку (Sku / количество, зашито в CargoReservationResponse) .
     *
     * @param requests объект с перечнем Sku (артикулов) и количеством товаров в бронь
     * @return объект CargoReservationResponse, содержащий id брони (резервирования)
     */
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
    }

    /**
     * 2️⃣ Снять резервирование (бронь) с товаров по ID бронирования.
     * Переводит бронь в неактивное состояние и возвращает товары в статус AVAILABLE.
     *
     * @param id идентификатор бронирования
     */
    @Transactional
    public void cancelReservation(Long id) {
        log.info("Запуск процесса отмены бронирования с id = {}", id);

        // 1. Ищем бронирование в БД
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Бронирование с id = %d не найдено", id)
                ));

        // 2. Проверяем, активна ли бронь на данный момент
        if (!reservation.getIsActive()) {
            log.warn("Попытка отменить уже неактивное бронирование с id = {}", id);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Данное бронирование уже было отменено ранее"
            );
        }

        // 3. Получаем список ID товаров, привязанных к этой брони
        Set<Long> cargoIds = reservation.getCargoIds();
        if (cargoIds != null && !cargoIds.isEmpty()) {
            // Извлекаем сущности Cargo из базы пачкой
            List<Cargo> reservedCargos = cargoRepository.findAllById(cargoIds);

            // Возвращаем каждый товар обратно в оборот склада
            for (Cargo cargo : reservedCargos) {
                if (cargo.getStatus() == Status.RESERVED) {
                    cargo.updateStatus(Status.AVAILABLE);
                }
            }

            cargoRepository.saveAll(reservedCargos);
        }

        // 4. Гасим саму бронь и сохраняем изменения
        reservation.setIsActive(false);
        reservationRepository.save(reservation);

        log.info("Бронирование id = {} успешно отменено. Товары в количестве {} шт. возвращены на склад.",
                id, reservation.getTotalQuantity());
    }

    /**
     * 3️⃣ Получить пагинированный список всех резервирований в системе.
     *
     * @param pageable параметры пагинации от контроллера
     * @return страница с DTO бронирований
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<CargoReservationResponse> getAllReservations(Pageable pageable) {
        Page<Reservation> reservationPage = reservationRepository.findAll(pageable);

        return reservationPage.map(r -> new CargoReservationResponse(
                r.getId(),
                r.getCreatedAt(),
                r.getTotalPrice(),
                r.getTotalWeight(),
                r.getTotalQuantity(),
                r.getCurrency()
        ));
    }

    /**
     * 4️⃣ Перевести забронированные товары в статус отгрузки (SHIPPED).
     * Проверяет, что товары действительно забронированы, меняет их статус и закрывает бронь.
     *
     * @param id идентификатор бронирования
     */
    @Transactional
    public void shipReservation(Long id) {
        log.info("Запуск процесса отгрузки товаров по бронированию с id = {}", id);

        // 1. Ищем бронирование в БД
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Бронирование с id = %d не найдено", id)
                ));

        // 2. Проверяем, активна ли бронь (нельзя отгрузить отмененную или уже отгруженную бронь)
        if (!reservation.getIsActive()) {
            log.warn("Попытка отгрузить неактивное бронирование с id = {}", id);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Невозможно отгрузить товары: данное бронирование неактивно (отменено или уже отгружено)"
            );
        }

        // 3. Извлекаем ID товаров и переводим их в статус SHIPPED
        Set<Long> cargoIds = reservation.getCargoIds();
        if (cargoIds != null && !cargoIds.isEmpty()) {
            List<Cargo> cargosToShip = cargoRepository.findAllById(cargoIds);

            for (Cargo cargo : cargosToShip) {
                // 🛑 Критическая валидация: товар обязан быть зарезервирован именно под эту операцию
                if (cargo.getStatus() != Status.RESERVED) {
                    log.error("Конфликт статуса! Товар id = {} имеет статус {}, ожидался RESERVED",
                            cargo.getId(), cargo.getStatus());
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            String.format("Товар с id = %d не может быть отгружен, так как он не забронирован (Текущий статус: %s)",
                                    cargo.getId(), cargo.getStatus())
                    );
                }
                // Переводим в статус SHIPPED
                cargo.updateStatus(Status.SHIPPED);
            }
            // Сохраняем обновленные товары пачкой
            cargoRepository.saveAll(cargosToShip);
        }

        // 4. Фиксируем, что стадия резервации завершена (гасим активность брони)
        reservation.setIsActive(false);
        reservationRepository.save(reservation);

        log.info("Бронирование id = {} успешно переведено в статус SHIPPED. Отгружено {} единиц товара.",
                id, reservation.getTotalQuantity());
    }
}

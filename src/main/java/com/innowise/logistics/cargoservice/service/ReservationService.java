package com.innowise.logistics.cargoservice.service;

import com.innowise.logistics.cargoservice.dto.request.CargoReservationRequest;
import com.innowise.logistics.cargoservice.dto.response.CargoReservationResponse;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Reservation;
import com.innowise.logistics.cargoservice.entity.Status;
import com.innowise.logistics.cargoservice.mapper.CargoMapper;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import com.innowise.logistics.cargoservice.repository.ReservationRepository;
import com.innowise.logistics.cargoservice.repository.SkuRepository;
import com.innowise.logistics.cargoservice.repository.projection.CargoReservationProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.innowise.logistics.cargoservice.constant.Constants.CURRENCY;

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
     * согласно переданному списку (Sku / количество).
     * <p>
     * Алгоритм:
     * 0. Валидация входных данных
     * 1. Формирование списка свободных к резервированию товаров (проекции)
     * 2. Сбор ID для bulk-обновления
     * 3. Обновление статуса одним запросом
     * 4. Создание бронирования
     * 5. Формирование ответа клиенту
     *
     * @param requests список артикулов и количеств для резервирования
     * @return объект CargoReservationResponse с деталями брони
     */
    @Transactional
    public CargoReservationResponse reserveItems(List<CargoReservationRequest> requests) {
        log.info("Попытка резервирования списка товаров из {} позиций.", requests.size());

        // 0. Валидация
        validateRequests(requests);

        // 1. Формируем список свободных к резервированию товаров
        List<CargoReservationProjection> projections = selectCargosForReservation(requests);

        // 2. Собираем ID для bulk-обновления
        List<Long> cargoIds = extractCargoIds(projections);

        // 3. Обновляем статус одним запросом (bulk-update)
        int updatedCount = updateCargosStatus(cargoIds, Status.RESERVED);
        validateUpdateCount(updatedCount, cargoIds.size());

        // 4. Создаём бронирование
        Reservation reservation = createReservation(projections, cargoIds);
        Reservation savedReservation = reservationRepository.save(reservation);

        log.info("Резервирование {} товаров успешно завершено. ID брони = {}",
                savedReservation.getTotalQuantity(), savedReservation.getId());

        // 5. Формируем ответ клиенту
        return buildResponse(savedReservation);
    }


    // === 1. Валидация запросов

    private void validateRequests(List<CargoReservationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Список артикулов товаров пуст");
        }

        for (CargoReservationRequest request : requests) {
            validateSingleRequest(request);
        }
    }

    private void validateSingleRequest(CargoReservationRequest request) {
        if (request.getSkuId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID артикула не может быть null");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Количество для артикула %d должно быть > 0", request.getSkuId())
            );
        }

        if (!skuRepository.existsById(request.getSkuId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Артикул с id = %d не существует", request.getSkuId())
            );
        }
    }


    // === 2. Выбор товаров для резервирования

    private List<CargoReservationProjection> selectCargosForReservation(List<CargoReservationRequest> requests) {
        List<CargoReservationProjection> allSelected = new ArrayList<>();

        for (CargoReservationRequest request : requests) {
            List<CargoReservationProjection> selectedForSku = selectCargosForSingleSku(request);
            allSelected.addAll(selectedForSku);
        }

        return allSelected;
    }

    private List<CargoReservationProjection> selectCargosForSingleSku(CargoReservationRequest request) {
        int requestedQuantity = request.getQuantity(); // сколько `Cargo` обязано быть зарезервировано

        // 1. Сначала пытаемся взять по предпочтительным/ желаемым ID
        List<CargoReservationProjection> selected = findByIdsIfPresent(request);

        // 2. Если не хватает — добираем по SKU (самые дешёвые)
        if (selected.size() < requestedQuantity) {
            selected.addAll(findAdditionalBySku(request, selected));
        }

        // 3. Проверяем, что набрали нужное количество
        validateSufficientQuantity(selected, request);

        return selected;
    }

    private List<CargoReservationProjection> findByIdsIfPresent(CargoReservationRequest request) {
        List<Long> preferredIds = request.getPreferredCargoIds();
        if (preferredIds == null || preferredIds.isEmpty()) {
            return new ArrayList<>();
        }

        return cargoRepository.findProjectionsByCargoIdAndStatus(
                preferredIds,
                Status.AVAILABLE,
                PageRequest.of(0, request.getQuantity())
        );
    }

    private List<CargoReservationProjection> findAdditionalBySku(
            CargoReservationRequest request,
            List<CargoReservationProjection> alreadyFound) {

        List<Long> excludedIds = alreadyFound.stream()
                .map(CargoReservationProjection::getId)
                .toList();

        int needed = request.getQuantity() - alreadyFound.size();

        return cargoRepository.findProjectionsBySkuIdAndStatusExcludingIds(
                request.getSkuId(),
                Status.AVAILABLE,
                excludedIds,
                PageRequest.of(0, needed, Sort.by("price").ascending())
        );
    }

    private void validateSufficientQuantity(
            List<CargoReservationProjection> selected,
            CargoReservationRequest request) {

        if (selected.size() < request.getQuantity()) {
            String message = String.format(
                    "Недостаточно товаров для SKU=%d. Запрошено: %d, доступно: %d",
                    request.getSkuId(),
                    request.getQuantity(),
                    selected.size()
            );
            log.error(message);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }


    // === 3. Обновление статусов (bulk)
    private List<Long> extractCargoIds(List<CargoReservationProjection> projections) {
        return projections.stream()
                .map(CargoReservationProjection::getId)
                .toList();
    }

    private int updateCargosStatus(List<Long> cargoIds, Status newStatus) {
        return cargoRepository.updateStatusByIds(cargoIds, newStatus);
    }

    private void validateUpdateCount(int updatedCount, int expectedCount) {
        if (updatedCount != expectedCount) {
            log.error("Несоответствие при обновлении статусов. Ожидалось: {}, обновлено: {}",
                    expectedCount, updatedCount);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при резервировании товаров. Попробуйте позже."
            );
        }
    }


    // === 4. Создание бронирования
    private Reservation createReservation(
            List<CargoReservationProjection> projections,
            List<Long> cargoIds) {

        Reservation reservation = new Reservation();
        reservation.setCargoIds(new HashSet<>(cargoIds));
        reservation.setIsActive(true);
        reservation.setTotalPrice(calculateTotalPrice(projections));
        reservation.setTotalWeight(calculateTotalWeight(projections));
        reservation.setTotalQuantity(projections.size());
        reservation.setCurrency(CURRENCY);

        return reservation;
    }

    private BigDecimal calculateTotalPrice(List<CargoReservationProjection> projections) {
        return projections.stream()
                .map(CargoReservationProjection::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Double calculateTotalWeight(List<CargoReservationProjection> projections) {
        double sum = projections.stream()
                .mapToDouble(CargoReservationProjection::getWeight)
                .sum();
        return BigDecimal.valueOf(sum)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }


    // === 5. Формирование ответа
    private CargoReservationResponse buildResponse(Reservation reservation) {
        return new CargoReservationResponse(
                reservation.getId(),
                reservation.getCreatedAt(),
                reservation.getIsActive(),
                reservation.getCargoIds(),
                reservation.getTotalQuantity(),
                reservation.getTotalWeight(),
                reservation.getTotalPrice(),
                reservation.getCurrency()
        );
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

        Reservation reservation = findActiveReservation(id);

        Set<Long> cargoIds = reservation.getCargoIds();
        if (cargoIds != null && !cargoIds.isEmpty()) {
            // ✅ Bulk-обновление — один запрос!
            int updated = cargoRepository.updateStatusByIds(
                    new ArrayList<>(cargoIds),
                    Status.AVAILABLE
            );
            log.info("Возвращено в AVAILABLE {} товаров", updated);
        }

        // Гасим бронь
        reservation.setIsActive(false);
        reservationRepository.save(reservation);

        log.info("Бронирование id = {} успешно отменено. Товары возвращены на склад.", id);
    }



    /**
     * 3️⃣ Получить пагинированный список всех резервирований в системе.
     * Можно отфильтровать по статусу активности (isActive).
     *
     * @param isActive фильтр по активности (true — активные, false — неактивные, null — все)
     * @param pageable параметры пагинации
     * @return страница с DTO бронирований
     */
    @Transactional(readOnly = true)
    public Page<CargoReservationResponse> getAllReservations(Boolean isActive, Pageable pageable) {
        Page<Reservation> reservationPage = reservationRepository.findAllByActive(isActive, pageable);

        return reservationPage.map(r -> new CargoReservationResponse(
                r.getId(),
                r.getCreatedAt(),
                r.getIsActive(),
                r.getCargoIds(),
                r.getTotalQuantity(),
                r.getTotalWeight(),
                r.getTotalPrice(),
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

        Reservation reservation = findActiveReservation(id);

        Set<Long> cargoIds = reservation.getCargoIds();
        if (cargoIds != null && !cargoIds.isEmpty()) {
            // Проверяем, что все товары действительно RESERVED
            List<Cargo> cargos = cargoRepository.findAllById(cargoIds);
            for (Cargo cargo : cargos) {
                if (cargo.getStatus() != Status.RESERVED) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            String.format("Товар id=%d имеет статус %s, ожидался RESERVED",
                                    cargo.getId(), cargo.getStatus())
                    );
                }
            }

            // ✅ Bulk-обновление — один запрос!
            int updated = cargoRepository.updateStatusByIds(
                    new ArrayList<>(cargoIds),
                    Status.SHIPPED
            );
            log.info("Переведено в SHIPPED {} товаров", updated);
        }

        // Гасим бронь
        reservation.setIsActive(false);
        reservationRepository.save(reservation);

        log.info("Бронирование id = {} успешно переведено в статус SHIPPED. Отгружено {} единиц.",
                id, reservation.getTotalQuantity());
    }

    private Reservation findActiveReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Бронирование с id = %d не найдено", id)
                ));

        if (!reservation.getIsActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Бронирование неактивно (уже отменено или завершено)"
            );
        }

        return reservation;
    }
}

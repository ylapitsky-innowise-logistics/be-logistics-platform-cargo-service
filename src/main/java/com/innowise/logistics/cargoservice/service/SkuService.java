package com.innowise.logistics.cargoservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.logistics.cargoservice.dto.SkuStats;
import com.innowise.logistics.cargoservice.dto.request.SkuCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.SkuUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.CargoViewResponse;
import com.innowise.logistics.cargoservice.dto.response.SkuAvailabilityResponse;
import com.innowise.logistics.cargoservice.dto.response.SkuCreatingResponse;
import com.innowise.logistics.cargoservice.dto.response.SkuResponse;
import com.innowise.logistics.cargoservice.dto.response.SkuViewResponse;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Sku;
import com.innowise.logistics.cargoservice.entity.Status;
import com.innowise.logistics.cargoservice.mapper.CargoMapper;
import com.innowise.logistics.cargoservice.mapper.SkuAvailabilityMapper;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import com.innowise.logistics.cargoservice.repository.ReservationRepository;
import com.innowise.logistics.cargoservice.repository.SkuRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 🟢 Глобальная оптимизация чтения на уровне класса
public class SkuService {

    private final CargoRepository cargoRepository;
    private final ReservationRepository reservationRepository;
    private final SkuRepository skuRepository;
    private final CargoMapper cargoMapper;
    private final SkuAvailabilityMapper skuAvailabilityMapper;

    // =========================================================
    // СТАНДАРТНЫЕ CRUD ОПЕРАЦИИ
    // =========================================================

    /**
     * C - Create: Заведение нового артикула (SKU) в систему
     */
    @Transactional
    public SkuCreatingResponse createSku(SkuCreatingRequest request) {
        log.info("Создание нового артикула с именем: {}", request.getName());
        Sku sku = new Sku();
        sku.setName(request.getName()); // Сработает кастомный тримминг пробелов
        sku.setDescription(request.getDescription());
        sku.setActive(true); // Новый артикул активен по умолчанию

        Sku savedSku = skuRepository.save(sku);
        return new SkuCreatingResponse(savedSku.getId());
    }

    /**
     * R - Read: Получение полной карточки артикула по ID
     */
    public SkuViewResponse getSkuById(Long id) {
        log.debug("Получение полной информации по SKU ID: {}", id);
        return skuRepository.findById(id)
                .map(this::mapToViewResponse)
                .orElseThrow(() -> new EntityNotFoundException("Артикул (SKU) с ID " + id + " не найден"));
    }

    /**
     * U - Update: Обновление параметров существующего артикула
     */
    @Transactional
    public SkuViewResponse updateSku(Long id, SkuUpdateRequest request) {
        log.info("Обновление артикула с ID: {}", id);
        Sku sku = skuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Артикул (SKU) с ID " + id + " не найден"));

        sku.setName(request.getName()); // Кастомный сеттер очистит пробелы
        sku.setDescription(request.getDescription());
        sku.setActive(request.isActive());

        return mapToViewResponse(sku);
    }

    /**
     * D - Delete: Удаление артикула из системы по ID
     */
    @Transactional
    public void deleteSku(Long id) {
        log.warn("Запрос на удаление артикула с ID: {}", id);
        if (!skuRepository.existsById(id)) {
            throw new EntityNotFoundException("Артикул (SKU) с ID " + id + " не найден");
        }
        skuRepository.deleteById(id);
    }

    // =========================================================
    // 📊 БЛОК АНАЛИТИКИ И СПЕЦИАЛЬНЫХ ВЫБОРОК (СОХРАНЕН ПОЛНОСТЬЮ)
    // =========================================================

    /**
     * 1️⃣ Просмотр агрегированной статистики по всем УНИКАЛЬНЫМ доступным SKU.
     */
    public Page<SkuAvailabilityResponse> getAvailableSkusByActive(Boolean isActive, Pageable pageable) {
//        return cargoRepository.findAvailableSkuStats(Status.AVAILABLE, pageable);
        log.debug("Getting SKU availability with isActive: {}", isActive);

        // Один запрос к БД с группировкой
        Page<SkuStats> statsPage = cargoRepository.findSkuStatsByStatusAndActive(
                Status.AVAILABLE,
                isActive,
                pageable
        );

        // Маппим результат
        return statsPage.map(skuAvailabilityMapper::toResponse);
    }

    /**
     * 2️⃣ Получить доступные товары по SKU с динамической пагинацией и гибкой сортировкой.
     */
    public Page<CargoViewResponse> getAvailableItemsBySku(Long skuId, int pageNum, int pageSize, String sortBy) {
        Sort sort = resolveSortOrder(sortBy);
        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
        Page<Cargo> cargoPage = cargoRepository.findBySkuIdAndStatus(skuId, Status.AVAILABLE, pageable);
        return cargoPage.map(cargoMapper::toDto);
    }

    /**
     * 3️⃣ Получить плоский пагинированный список абсолютно всех зарегистрированных в системе SKU.
     */
    public Page<SkuResponse> getSkus(Pageable pageable) {
        Page<Sku> skuPage = skuRepository.findAll(pageable);
        return skuPage.map(sku -> new SkuResponse(sku.getId(), sku.getName()));
    }

    // =========================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =========================================================

    private SkuViewResponse mapToViewResponse(Sku sku) {
        return new SkuViewResponse(
                sku.getId(),
                sku.getName(),
                sku.getDescription(),
                sku.isActive(),
                sku.getCreatedAt(),
                sku.getUpdatedAt()
        );
    }

    private Sort resolveSortOrder(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by("id").ascending();
        }
        return switch (sortBy.toLowerCase()) {
            case "price_asc"  -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "created_asc"  -> Sort.by("createdAt").ascending();
            case "created_desc" -> Sort.by("createdAt").descending();
            case "status_date_asc"  -> Sort.by("statusAt").ascending();
            case "status_date_desc" -> Sort.by("statusAt").descending();
            case "status" -> Sort.by("status").ascending();
            case "location_rack" -> Sort.by("location.rack").ascending();
            case "location_shelf" -> Sort.by("location.shelf").ascending();
            default -> Sort.by("id").ascending();
        };
    }
}

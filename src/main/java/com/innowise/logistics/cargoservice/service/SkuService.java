package com.innowise.logistics.cargoservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.logistics.cargoservice.dto.response.CargoViewResponse;
import com.innowise.logistics.cargoservice.dto.response.SkuAvailabilityResponse;
import com.innowise.logistics.cargoservice.dto.response.SkuResponse;
import com.innowise.logistics.cargoservice.entity.Cargo;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkuService {

    private final CargoRepository cargoRepository;
    private final ReservationRepository reservationRepository;
    private final SkuRepository skuRepository;
    private final CargoMapper cargoMapper;
    private final ObjectMapper objectMapper;

    /**
     * 1️⃣ Просмотр агрегированной статистики по всем УНИКАЛЬНЫХ доступным SKU.
     * @param pageable параметры пагинации
     * @return агрегированный список, зашитый в 'SkuAvailabilityResponse'
     */
    @Transactional(readOnly = true)
    public Page<SkuAvailabilityResponse> getAvailableSkus(Pageable pageable) {
        return cargoRepository.findAvailableSkuStats(
                Status.AVAILABLE,
                pageable
        );
    }

    /**
     * 2️⃣ Получить доступные товары по SKU с динамической пагинацией и гибкой сортировкой.
     * Получить детальный пагинированный список конкретных доступных грузов по ID артикула.
     * Поддерживает гибкую кастомною сортировку (8 видов) через текстовый параметр sortBy.
     * @param skuId   идентификатор артикула
     * @param pageNum   номер страницы (начиная с 0)
     * @param pageSize  размер страницы
     * @param sortBy    строковый код сортировки (например, "price_asc", "created_desc")
     */
    @Transactional(readOnly = true)
    public Page<CargoViewResponse> getAvailableItemsBySku(Long skuId, int pageNum, int pageSize, String sortBy) {

        // 1. Собираем объект Sort на основе переданного строкового критерия
        Sort sort = resolveSortOrder(sortBy);

        // 2. Объединяем пагинацию и нашу динамическую сортировку
        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

        // 3. Выполняем запрос в репозиторий (там отработает наш FETCH JOIN) // Вычитываем пагинированную страницу сущностей из репозитория
        Page<Cargo> cargoPage = cargoRepository.findBySkuIdAndStatus(skuId, Status.AVAILABLE, pageable);

        // 4. Мапим через ваш MapStruct маппер в CargoViewResponse
        return cargoPage.map(cargoMapper::toDto);
    }

    /**
     * Вспомогательный метод-резолвер для маппинга бизнес-критериев в технический Sort.
     * Промышленный Senior-стандарт разделения логики.
     */
    private Sort resolveSortOrder(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by("id").ascending(); // Дефолтная сортировка по ID
        }

        return switch (sortBy.toLowerCase()) {
            // 1 & 2. Сортировка по стоимости
            case "price_asc"  -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();

            // 3 & 4. Сортировка по дате поступления
            case "created_asc"  -> Sort.by("createdAt").ascending();
            case "created_desc" -> Sort.by("createdAt").descending();

            // 5 & 6. Сортировка по дате изменения статуса
            case "status_date_asc"  -> Sort.by("statusAt").ascending();
            case "status_date_desc" -> Sort.by("statusAt").descending();

            // 7. Сортировка по самому статусу
            case "status" -> Sort.by("status").ascending();

            // 8. Доп. вариант: Сортировка по вложенному объекту (например, по названию стеллажа)
            case "location_rack" -> Sort.by("location.rack").ascending();
            case "location_shelf" -> Sort.by("location.shelf").ascending();

            // На случай, если прилетело что-то неизвестное
            default -> Sort.by("id").ascending();
        };
    }

    /**
     * 3️⃣ Получить плоский пагинированный список абсолютно всех зарегистрированных в системе SKU (артикулов).
     */
    @Transactional(readOnly = true)
    public Page<SkuResponse> getSkus(Pageable pageable) {
        // 1. Запрашиваем страницу сущностей Sku из правильного репозитория
        Page<Sku> skuPage = skuRepository.findAll(pageable);

        // 2. Маппим Entity в DTO "на лету" через чистый конструктор рекорда
        return skuPage.map(sku -> new SkuResponse(sku.getId(), sku.getName()));
    }
}

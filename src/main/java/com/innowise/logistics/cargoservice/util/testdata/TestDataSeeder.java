package com.innowise.logistics.cargoservice.util.testdata;

import com.innowise.logistics.cargoservice.entity.*;
import com.innowise.logistics.cargoservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 🟢 Сервис-оркестратор наполнения базы данных тестовыми данными.
 * Открывает единую транзакцию и соблюдает строгую последовательность сохранения графа связей.
 * т.е. он выступает в роли диспетчера: вызывает генераторы по цепочке,
 * поочередно сохраняет сущности через репозитории и передает ID дальше.
 * ЧИСТЫЙ POJO КЛАСС. Управляется централизованно через конфигурацию.
 */
@Slf4j
@RequiredArgsConstructor
public class TestDataSeeder {

    // Чистые фабрики объектов в памяти (POJO)
    private final AddressGenerator addressGenerator;
    private final SkuGenerator skuGenerator;
    private final DimensionGenerator dimensionGenerator;
    private final LocationGenerator locationGenerator;
    private final CargoGenerator cargoGenerator;

    // Репозитории JPA для сохранения в PostgreSQL
    private final AddressRepository addressRepository;
    private final SkuRepository skuRepository;
    private final DimensionRepository dimensionRepository;
    private final LocationRepository locationRepository;
    private final CargoRepository cargoRepository;

    /**
     * Заполняет базу данных PostgreSQL сбалансированным графом связанных сущностей.
     * @param cargoQuantity желаемое количество физических коробок на складе
     */
    @Transactional // 🟢 Все сохранения выполняются в рамках одной накатываемой транзакции
    public void seedAllTestData(int cargoQuantity) {
        log.info("=== НАЧАЛО СИНХРОННОЙ ГЕНЕРАЦИИ И СОХРАНЕНИЯ ТЕСТОВЫХ ДАННЫХ ===");

        // 1. Генерируем и сохраняем базовые адреса складов (например, 10 крупных комплексов)
        Address[] rawAddresses = addressGenerator.generate(10);
        List<Address> savedAddresses = addressRepository.saveAll(List.of(rawAddresses));
        log.info("✓ Сохранено адресов складов: {}", savedAddresses.size());

        // 2. Генерируем ячейки хранения (locations) строго на базе сохраненных адресов
        int locationCount = Math.max(20, cargoQuantity / 10);
        Location[] rawLocations = locationGenerator.generateForAddresses(locationCount, savedAddresses.toArray(new Address[0]));
        List<Location> savedLocations = locationRepository.saveAll(List.of(rawLocations));
        log.info("✓ Сохранено складских ячеек: {}", savedLocations.size());

        // 3. Генерируем и сохраняем габариты упаковки (например, 15 уникальных типоразмеров коробок)
        Dimension[] rawDimensions = dimensionGenerator.generate(15);
        List<Dimension> savedDimensions = dimensionRepository.saveAll(List.of(rawDimensions));
        log.info("✓ Сохранено физических типоразмеров коробок: {}", savedDimensions.size());

        // 4. Генерируем и сохраняем эталонную матрицу из 20 SKU со скриншота
        Sku[] rawSkus = skuGenerator.generate(20);
        List<Sku> savedSkus = skuRepository.saveAll(List.of(rawSkus));
        log.info("✓ Сохранено каталожных артикулов (SKU): {}", savedSkus.size());

        // 5. Вызываем конвейер сборки Cargo, но передаем туда данные, которые мы УЖЕ сохранили
        // Для этого мы немного доработаем вызов или перегрузим CargoGenerator
        Cargo[] rawCargos = cargoGenerator.generateWithSavedEntities(
                cargoQuantity,
                savedSkus.toArray(new Sku[0]),
                savedDimensions.toArray(new Dimension[0]),
                savedLocations.toArray(new Location[0])
        );

        List<Cargo> savedCargos = cargoRepository.saveAll(List.of(rawCargos));
        log.info("=== УСПЕШНО. База PostgreSQL наполнена. Физических товаров (Cargo) сохранено: {} ===", savedCargos.size());
    }
}

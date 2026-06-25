package com.innowise.logistics.cargoservice.util.testdata;

import com.innowise.logistics.cargoservice.entity.*;
import com.innowise.logistics.cargoservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 🟢 Сервис-оркестратор наполнения базы данных тестовыми данными.
 * Открывает единую транзакцию и соблюдает строгую последовательность сохранения графа связей.
 * т.е. он выступает в роли диспетчера: вызывает генераторы по цепочке,
 * поочередно сохраняет сущности через репозитории и передает ID дальше.
 * Поддерживает идемпотентность: повторные запуски не вызывают конфликтов unique-ключей.
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
     * Метод полностью безопасен для повторных вызовов.
     * @param cargoQuantity желаемое количество физических коробок на складе
     */
    @Transactional
    public void seedAllTestData(int cargoQuantity, Boolean isCleanUp) {
        log.info("=== НАЧАЛО СИНХРОННОЙ ГЕНЕРАЦИИ И СОХРАНЕНИЯ ТЕСТОВЫХ ДАННЫХ ===");

        if (isCleanUp) {  // нужно ли предварительно очищать всю БД
            cleanDatabase();
        }

        // 1. Генерируем и сохраняем базовые адреса складов
        Address[] rawAddresses = addressGenerator.generate(10);
        List<Address> savedAddresses = addressRepository.saveAll(List.of(rawAddresses));
        log.info("✓ Успешно сохранено адресов складов в DB: {}", savedAddresses.size());

        // 2. Генерируем ячейки хранения на базе сохраненных адресов
        int locationCount = Math.max(20, cargoQuantity / 10);
        Location[] rawLocations = locationGenerator.generateForAddresses(locationCount, savedAddresses.toArray(new Address[0]));
        List<Location> savedLocations = locationRepository.saveAll(List.of(rawLocations));
        log.info("✓ Успешно сохранено складских ячеек в DB: {}", savedLocations.size());

        // 3. Генерируем и сохраняем габариты упаковки
        Dimension[] rawDimensions = dimensionGenerator.generate(15);
        List<Dimension> savedDimensions = dimensionRepository.saveAll(List.of(rawDimensions));
        log.info("✓ Успешно сохранено физических типоразмеров коробок в DB: {}", savedDimensions.size());

        // 4. 🎯 УМНОЕ СОХРАНЕНИЕ SKU (Защита от uq_sku_name)
        Sku[] rawSkus = skuGenerator.generate(20);
        List<Sku> savedSkus = new ArrayList<>();

        // Достаем все существующие в базе SKU один раз, чтобы не спамить БД запросами в цикле
        Map<String, Sku> existingSkusMap = skuRepository.findAll().stream()
                .collect(Collectors.toMap(Sku::getName, sku -> sku));

        for (Sku rawSku : rawSkus) {
            if (existingSkusMap.containsKey(rawSku.getName())) {
                // Если артикул с таким кодом уже есть в Postgres — берем его из базы
                savedSkus.add(existingSkusMap.get(rawSku.getName()));
            } else {
                // Если артикул новый — сохраняем в базу
                savedSkus.add(skuRepository.save(rawSku));
            }
        }
        log.info("✓ Обработано каталожных артикулов (SKU). Итого в обойме: {}", savedSkus.size());

        // 5. Вызываем конвейер сборки Cargo на базе объектов, уже имеющих первичные ключи PostgreSQL
        Cargo[] rawCargos = cargoGenerator.generateWithSavedEntities(
                cargoQuantity,
                savedSkus.toArray(new Sku[0]),
                savedDimensions.toArray(new Dimension[0]),
                savedLocations.toArray(new Location[0])
        );

        List<Cargo> savedCargos = cargoRepository.saveAll(List.of(rawCargos));
        log.info("=== УСПЕШНО ЗАВЕРШЕНО. Физических товаров (Cargo) добавлено в DB: {} ===", savedCargos.size());
    }

    private void cleanDatabase() {
        cargoRepository.deleteAll();
        locationRepository.deleteAll();
        addressRepository.deleteAll();
        skuRepository.deleteAll();
        dimensionRepository.deleteAll();
    }
}

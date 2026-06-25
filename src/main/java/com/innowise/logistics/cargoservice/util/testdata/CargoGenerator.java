package com.innowise.logistics.cargoservice.util.testdata;

import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Category;
import com.innowise.logistics.cargoservice.entity.Dimension;
import com.innowise.logistics.cargoservice.entity.Location;
import com.innowise.logistics.cargoservice.entity.Sku;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ⚖️ Центральный Конвейер Грузов
 * Объединяет логику генерации в оперативной памяти и генерации на базе персистентных ID сущностей.
 */
@Slf4j
@RequiredArgsConstructor
public class CargoGenerator implements Generator<Cargo> {

    // Внедряем смежные генераторы для комплексной сборки агрегата Cargo
    private final SkuGenerator skuGenerator;
    private final DimensionGenerator dimensionGenerator;
    private final LocationGenerator locationGenerator;

    /**
     * 1️⃣ Базовый метод конвейера генерации физических грузов (в оперативной памяти).
     * Внимание: Сгенерированные здесь Sku, Dimension и Location не имеют ID базы данных.
     */
    @Override
    public Cargo[] generate(int quantity) {
        log.info("Запуск конвейера генерации {} физических единиц груза (Cargo) в памяти", quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Количество генерируемых грузов должно быть целым положительным числом. Введено: " + quantity);
        }

        // 1. Подготавливаем эталонные 20 артикулов со скриншота
        Sku[] baseSkus = skuGenerator.generate(20);

        // 2. Строим карту "природы товара" для SKU, чтобы свойства не "плыли" между коробками
        Map<String, CargoNatureProfile> natureCache = buildCargoNatureCache(baseSkus);

        // 3. Создаем пул складских ячеек (например, 1 ячейка на каждые 10 коробок, но не менее 20)
        int locationPoolSize = Math.max(20, quantity / 10);
        Location[] locationPool = locationGenerator.generate(locationPoolSize);

        Cargo[] cargos = new Cargo[quantity];
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 4. Заполняем склады тысячами коробок
        for (int i = 0; i < quantity; i++) {
            Sku randomSku = baseSkus[random.nextInt(baseSkus.length)];
            CargoNatureProfile profile = natureCache.get(randomSku.getName());

            Cargo cargo = new Cargo();

            // Заполняем неизменяемые поля («природу») строго из кэша артикула
            cargo.setSku(randomSku);
            cargo.setName(profile.cargoName());
            cargo.setCategory(profile.category());
            cargo.setWeight(profile.weight());
            cargo.setDimension(profile.dimension());

            cargo.setPrice(generateRealisticPrice(profile.category()));

            Location randomLocation = locationPool[random.nextInt(locationPool.length)];
            cargo.setLocation(randomLocation);

            cargos[i] = cargo;
        }

        log.info("Конвейер в памяти успешно завершен. Сформировано {} товаров", quantity);
        return cargos;
    }

    /**
     * Генерация Cargo на базе УЖЕ СОХРАНЕННЫХ в базе данных (PostgreSQL) сущностей.
     * Полностью исключает TransientObjectException, так как все связанные объекты уже имеют ID.
     */
    public Cargo[] generateWithSavedEntities(int quantity, Sku[] savedSkus, Dimension[] savedDimensions, Location[] savedLocations) {
        log.info("Запуск конвейера генерации {} Cargo на базе сохраненных сущностей DB", quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Количество генерируемых грузов должно быть целым положительным числом. Введено: " + quantity);
        }
        if (savedSkus == null || savedSkus.length == 0 || savedDimensions == null || savedDimensions.length == 0 || savedLocations == null || savedLocations.length == 0) {
            throw new IllegalArgumentException("Массивы сохраненных сущностей-зависимостей не могут быть пустыми");
        }

        // Строим карту природы товара, привязывая артикулы к уже существующим в базе габаритам
        Map<String, CargoNatureProfile> natureCache = buildCargoNatureCacheWithSavedDimensions(savedSkus, savedDimensions);

        Cargo[] cargos = new Cargo[quantity];
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < quantity; i++) {
            // Случайно выбираем один из сохраненных артикулов
            Sku randomSku = savedSkus[random.nextInt(savedSkus.length)];
            CargoNatureProfile profile = natureCache.get(randomSku.getName());

            Cargo cargo = new Cargo();

            // Накатываем эталонные физические свойства из кэша природы артикула
            cargo.setSku(randomSku);
            cargo.setName(profile.cargoName());
            cargo.setCategory(profile.category());
            cargo.setWeight(profile.weight());
            cargo.setDimension(profile.dimension()); // Валидный сохраненный габарит с ID

            // Генерируем коммерческие свойства конкретной коробки
            cargo.setPrice(generateRealisticPrice(profile.category()));

            // Размещаем коробку в случайную сохраненную ячейку на складе
            Location randomLocation = savedLocations[random.nextInt(savedLocations.length)];
            cargo.setLocation(randomLocation); // Валидная сохраненная ячейка с ID

            cargos[i] = cargo;
        }

        log.info("Конвейер генерации на базе DB успешно завершен. Сформировано {} товаров", quantity);
        return cargos;
    }

    // =========================================================
    // ВСПОРМАГАТЕЛЬНАЯ ТЕХНОЛОГИЧЕСКАЯ ЛОГИКА СБОРКИ
    // =========================================================

    private Map<String, CargoNatureProfile> buildCargoNatureCache(Sku[] skus) {
        Map<String, CargoNatureProfile> cache = new HashMap<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (Sku sku : skus) {
            Category category = resolveCategoryBySkuName(sku.getName());
            Dimension dimension = dimensionGenerator.generate(1)[0];

            double weight = switch (category) {
                case ELECTRONICS -> random.nextDouble(0.1, 3.5);
                case BOOKS       -> random.nextDouble(0.3, 1.8);
                case SPORTS      -> random.nextDouble(0.5, 25.0);
                case OTHER       -> random.nextDouble(1.0, 45.0);
            };
            weight = BigDecimal.valueOf(weight).setScale(2, RoundingMode.HALF_UP).doubleValue();
            String cargoName = sku.getDescription();

            cache.put(sku.getName(), new CargoNatureProfile(cargoName, category, weight, dimension));
        }
        return cache;
    }

    /**
     * ХЕЛПЕР: Строит кэш профилей, распределяя артикулы по переданным СОХРАНЕННЫМ габаритам.
     */
    private Map<String, CargoNatureProfile> buildCargoNatureCacheWithSavedDimensions(Sku[] skus, Dimension[] savedDimensions) {
        Map<String, CargoNatureProfile> cache = new HashMap<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (Sku sku : skus) {
            Category category = resolveCategoryBySkuName(sku.getName());

            // Распределяем артикул на один из существующих в базе габаритов
            Dimension randomSavedDimension = savedDimensions[random.nextInt(savedDimensions.length)];

            double weight = switch (category) {
                case ELECTRONICS -> random.nextDouble(0.1, 3.5);
                case BOOKS       -> random.nextDouble(0.3, 1.8);
                case SPORTS      -> random.nextDouble(0.5, 25.0);
                case OTHER       -> random.nextDouble(1.0, 45.0);
            };
            weight = BigDecimal.valueOf(weight).setScale(2, RoundingMode.HALF_UP).doubleValue();
            String cargoName = sku.getDescription();

            cache.put(sku.getName(), new CargoNatureProfile(cargoName, category, weight, randomSavedDimension));
        }
        return cache;
    }

    private Category resolveCategoryBySkuName(String skuName) {
        if (skuName.startsWith("ELEC")) return Category.ELECTRONICS;
        if (skuName.startsWith("BOOK")) return Category.BOOKS;
        if (skuName.startsWith("SPRT")) return Category.SPORTS;
        return Category.OTHER;
    }

    private BigDecimal generateRealisticPrice(Category category) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double rawPrice = switch (category) {
            case ELECTRONICS -> random.nextDouble(1500.0, 120000.0);
            case BOOKS       -> random.nextDouble(400.0, 3500.0);
            case SPORTS      -> random.nextDouble(800.0, 15000.0);
            case OTHER       -> random.nextDouble(1200.0, 45000.0);
        };
        return BigDecimal.valueOf(rawPrice).setScale(2, RoundingMode.HALF_UP);
    }

    private record CargoNatureProfile(
            String cargoName,
            Category category,
            Double weight,
            Dimension dimension
    ) {}
}
package com.innowise.logistics.cargoservice.util.testdata;

import com.innowise.logistics.cargoservice.entity.Address;
import com.innowise.logistics.cargoservice.entity.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 🏢 Генератор Складских Ячеек
 * Связывает ячейки со складами без паразитного размножения лишних адресов.
 */
@Slf4j
@RequiredArgsConstructor
public class LocationGenerator implements Generator<Location> {

    // Внедряем генератор адресов для построения каскадной структуры данных
    private final AddressGenerator addressGenerator;

    /**
     * 1️⃣ Реализация базового метода интерфейса.
     * Автоматически создает реалистичную топологию: генерирует несколько складов
     * и распределяет между ними заданное количество ячеек хранения.
     */
    @Override
    public Location[] generate(int quantity) {
        log.info("Запрос на генерацию {} складских ячеек (locations)", quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Количество генерируемых локаций должно быть целым положительным числом. Введено: " + quantity);
        }

        // Вычисляем оптимальный пул складов (например, 1 склад на каждые 100 ячеек, но в пределах от 1 до 5)
        int warehouseCount = Math.max(1, Math.min(5, quantity / 100));
        log.debug("Для {} ячеек будет сформирован пул из {} уникальных адресов складов", quantity, warehouseCount);

        // Генерируем сами адреса
        Address[] warehouseAddresses = addressGenerator.generate(warehouseCount);

        // Распределяем ячейки по сгенерированным адресам
        return generateForAddresses(quantity, warehouseAddresses);
    }

    /**
     * 2️⃣ Продвинутый инженерный метод (Overloading).
     * Позволяет сгенерировать ячейки и жестко привязать их к ОПРЕДЕЛЕННЫМ адресам.
     * Незаменим в @SpringBootTest, когда адреса уже сохранены в Postgres и имеют валидные ID.
     */
    public Location[] generateForAddresses(int quantity, Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            throw new IllegalArgumentException("Список адресов складов не может быть пустым для генерации ячеек");
        }

        Location[] locations = new Location[quantity];
        for (int i = 0; i < quantity; i++) {
            Location location = new Location();

            // Генерируем понятную координату стеллажа (например: Стеллаж A-14, Стеллаж X-2)
            location.setRack(generateRackName());

            // Генерируем координату полки по высоте (обычно от 1 до 6 уровня)
            location.setShelf(generateShelfName());

            // Случайно связываем ячейку с одним из доступных складов из пула
            Address randomWarehouse = addresses[ThreadLocalRandom.current().nextInt(addresses.length)];
            location.setAddress(randomWarehouse);

            locations[i] = location;
        }

        log.info("Успешно сгенерировано {} ячеек, распределенных по {} складам", quantity, addresses.length);
        return locations;
    }

    // =========================================================
    // ВНУТРЕННЯЯ МАТРИЦА ГЕНЕРАЦИИ КООРДИНАТ СКЛАДА
    // =========================================================

    private String generateRackName() {
        // Логистические зоны на больших складах (A, B, C...)
        String[] zones = {"A", "B", "C", "D", "X", "Y"};
        String zone = zones[ThreadLocalRandom.current().nextInt(zones.length)];
        // Номер пролета/ряда стеллажей
        int rackNumber = ThreadLocalRandom.current().nextInt(1, 30);

        return "Стеллаж " + zone + "-" + rackNumber;
    }

    private String generateShelfName() {
        // Полки вертикального яруса (от 1 до 6 этажа конструкции стеллажа)
        int shelfLevel = ThreadLocalRandom.current().nextInt(1, 7);
        return "Полка " + shelfLevel;
    }
}

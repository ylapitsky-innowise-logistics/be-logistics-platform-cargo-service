package com.innowise.logistics.cargoservice.util.testdata;

import com.innowise.logistics.cargoservice.entity.Sku;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 📦 Генератор Каталога Артикулов
 * Жестко фиксирует 20 эталонных позиций с твоего скриншота.
 */
@Slf4j
@RequiredArgsConstructor
public class SkuGenerator implements Generator<Sku> {

    // Внутренний контейнер для жестко зафиксированных шаблонов с картинками
    private final List<SkuTemplate> templates = new ArrayList<>();

    /**
     * Инициализация базовой матрицы артикулов строго по скриншоту.
     * префиксы категорий + уникальные коды моделей.
     */
    @PostConstruct
    public void init() {
        log.info("Инициализация статического каталога артикулов на основе медиа-пакета");

        // 🎧 Электроника и гаджеты (Префикс ELEC)
        templates.add(new SkuTemplate("ELEC-WHP-001", "Беспроводные наушники"));
        templates.add(new SkuTemplate("ELEC-LMP-002", "Настольная лампа светодиодная"));
        templates.add(new SkuTemplate("ELEC-LPT-003", "Ноутбук рабочий"));
        templates.add(new SkuTemplate("ELEC-PHN-004", "Смартфон премиум"));
        templates.add(new SkuTemplate("ELEC-HUM-005", "Увлажнитель воздуха ультразвуковой"));
        templates.add(new SkuTemplate("ELEC-WCH-006", "Умные часы"));

        // 📚 Книжная продукция (Префикс BOOK)
        templates.add(new SkuTemplate("BOOK-ENT-001", "Книга Паттерны проектирования Enterprise-приложений"));
        templates.add(new SkuTemplate("BOOK-SBT-002", "Книга Руководство по Spring Boot 3"));
        templates.add(new SkuTemplate("BOOK-JPH-003", "Книга Философия Java"));
        templates.add(new SkuTemplate("BOOK-ARC-004", "Книга Чистая Архитектура"));

        // 🏋️‍♂️ Спортивные товары (Префикс SPRT)
        templates.add(new SkuTemplate("SPRT-YGM-001", "Коврик для йоги нескользящий"));
        templates.add(new SkuTemplate("SPRT-DBL-002", "Набор разборных гантелей"));
        templates.add(new SkuTemplate("SPRT-BPK-003", "Спортивный рюкзак водонепроницаемый"));
        templates.add(new SkuTemplate("SPRT-BLL-004", "Футбольный мяч турнирный"));

        // 🗄️ Офис и Складская мебель (Префикс FURN)
        templates.add(new SkuTemplate("FURN-CHR-001", "Кресло офисное анатомическое"));
        templates.add(new SkuTemplate("FURN-SHV-002", "Стеллаж металлический складской"));
        templates.add(new SkuTemplate("FURN-DSK-003", "Стол письменный эргономичный"));

        // 💼 Канцелярия и быт (Префикс OTHR)
        templates.add(new SkuTemplate("OTHR-STN-001", "Канцелярский набор премиум"));
        templates.add(new SkuTemplate("OTHR-UMB-002", "Компактный складной зонт"));
        templates.add(new SkuTemplate("OTHR-MUG-003", "Термокружка вакуумная"));
    }

    /**
     * Генерирует массив SKU заданной длины.
     * Если запрошено больше 20 шт, циклически создает уникальные модификации (V2, V3...).
     */
    @Override
    public Sku[] generate(int quantity) {
        log.info("Запрос на генерацию {} артикулов товаров", quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Количество генерируемых SKU должно быть целым положительным числом. Введено: " + quantity);
        }

        Sku[] skus = new Sku[quantity];
        int totalTemplates = templates.size();

        for (int i = 0; i < quantity; i++) {
            int templateIndex = i % totalTemplates;
            int versionFactor = i / totalTemplates;

            SkuTemplate template = templates.get(templateIndex);
            Sku sku = new Sku();

            if (versionFactor == 0) {
                // Базовые оригинальные позиции
                sku.setName(template.code()); // Кастомный сеттер сущности очистит пробелы
                sku.setDescription(template.description());
            } else {
                // Автоматическое масштабирование каталога без дублирования бизнес-ключей
                sku.setName(template.code() + "-V" + (versionFactor + 1));
                sku.setDescription(template.description() + " (Модификация " + (versionFactor + 1) + ")");
            }
            sku.setActive(true);
            skus[i] = sku;
        }

        return skus;
    }

    /**
     * Полезный метод для интеграционных тестов: получить ровно 20 эталонных
     * оригинальных SKU со скриншота в один вызов.
     */
    public List<Sku> generateAllStandardItems() {
        return List.of(generate(templates.size()));
    }

    /**
     * Вспомогательный immutable-компонент для хранения матрицы паттернов.
     */
    private record SkuTemplate(String code, String description) {}
}
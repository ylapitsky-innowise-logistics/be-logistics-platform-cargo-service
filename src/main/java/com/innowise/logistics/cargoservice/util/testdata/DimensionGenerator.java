package com.innowise.logistics.cargoservice.util.testdata;

import com.innowise.logistics.cargoservice.entity.Dimension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 📐 Генератор Физических Размеров
 * Генерирует сбалансированные коробки с жестким округлением до 0.1.
 */
@Slf4j
@RequiredArgsConstructor
public class DimensionGenerator implements Generator<Dimension> {

    @Override
    public Dimension[] generate(int quantity) {
        log.info("Запрос на генерацию {} уникальных габаритных размеров (dimensions)", quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Количество генерируемых габаритов должно быть целым положительным числом. Введено: " + quantity);
        }

        Dimension[] dimensions = new Dimension[quantity];
        for (int i = 0; i < quantity; i++) {
            dimensions[i] = generateSingleDimension();
        }

        return dimensions;
    }

    /**
     * Внутренняя логика генерации одного физического размера с распределением по типам груза.
     */
    private Dimension generateSingleDimension() {
        Dimension dimension = new Dimension();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Случайно выбираем один из 3 типов упаковки для реалистичности пропорций
        int packageType = random.nextInt(1, 4);

        double rawLength;
        double rawWidth;
        double rawHeight;

        switch (packageType) {
            case 1 -> { // 📱 Маленькая коробка (гаджеты, канцелярия, книги)
                rawLength = random.nextDouble(15.0, 35.0);
                rawWidth = random.nextDouble(10.0, 25.0);
                rawHeight = random.nextDouble(2.0, 12.0);
            }
            case 2 -> { // 🎒 Средняя коробка (ноутбуки, рюкзаки, термокружки)
                rawLength = random.nextDouble(40.0, 65.0);
                rawWidth = random.nextDouble(30.0, 50.0);
                rawHeight = random.nextDouble(15.0, 35.0);
            }
            default -> { // 🗄️ Крупный груз (кресла, столы, стеллажи)
                rawLength = random.nextDouble(80.0, 140.0);
                rawWidth = random.nextDouble(60.0, 90.0);
                rawHeight = random.nextDouble(50.0, 190.0);
            }
        }

        // Применяем жесткое округление до 0.1 перед установкой значений в сущность
        dimension.setLength(roundToTenth(rawLength));
        dimension.setWidth(roundToTenth(rawWidth));
        dimension.setHeight(roundToTenth(rawHeight));

        return dimension;
    }

    /**
     * Вспомогательный утилитный метод для точного математического округления до 0.1
     */
    private double roundToTenth(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

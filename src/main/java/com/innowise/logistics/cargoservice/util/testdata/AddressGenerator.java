package com.innowise.logistics.cargoservice.util.testdata;

import com.innowise.logistics.cargoservice.entity.Address;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 🌍 Генератор Адресов
 * Чистый класс. Аннотация @PostConstruct сработает штатно, так как объект будет управляться Spring через конфигурационный класс
 */
@Slf4j
@RequiredArgsConstructor
public class AddressGenerator implements Generator<Address> {

    // Массив доступных стран
    private final String[] countries = {"Беларусь", "Россия", "Польша", "Литва", "Украина"};

    // Пул Фейкеров под каждую страну, чтобы не плодить тяжелые объекты в цикле
    private final Map<String, Faker> fakerPool = new HashMap<>();


    @PostConstruct
    public void init() {
        log.info("Инициализация пула локализованных Faker для генерации адресов");
        fakerPool.put("Беларусь", new Faker(new Locale("be"))); // или "ru" для русскоязычной РБ
        fakerPool.put("Россия", new Faker(new Locale("ru")));
        fakerPool.put("Польша", new Faker(new Locale("pl")));
        fakerPool.put("Литва", new Faker(new Locale("lt")));
        fakerPool.put("Украина", new Faker(new Locale("uk")));
    }

    @Override
    public Address[] generate(int quantity) {
        log.info("Генерация {} валидных адресов", quantity);
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Количество генерируемых адресов должно быть целым положительным числом. Введено: " + quantity);
        }

        Address[] addresses = new Address[quantity];
        for (int i = 0; i < quantity; i++) {
            addresses[i] = generateSingleAddress();
        }
        return addresses;
    }

    private Address generateSingleAddress() {
        Address address = new Address();

        // 1. Случайно выбираем страну из списка
        String country = countries[ThreadLocalRandom.current().nextInt(countries.length)];
        address.setCountry(country);

        // 2. Достаем из пула соответствующий этой стране Faker
        Faker countryFaker = fakerPool.get(country);

        // 3. Генерируем контекстно-зависимые данные (они строго соответствуют выбранной стране!)
        address.setCity(countryFaker.address().city());

        address.setStreet(countryFaker.address().streetName());

        // Почтовый индекс (будет в формате выбранной страны, например 00-001 для Польши)
        String zip = countryFaker.address().zipCode();
        address.setZipCode(zip.length() > 10 ? zip.substring(0, 10) : zip);

        // Номер дома
        address.setHouse(countryFaker.number().numberBetween(1, 100));

        // Микрорайон (генерируем только для СНГ-региона, так как в pl/lt локалях префиксы могут отработать некрасиво)
        if (("Россия".equals(country) || "Беларусь".equals(country)) && ThreadLocalRandom.current().nextBoolean()) {
            address.setMicrodistrict(countryFaker.address().cityPrefix() + " " + countryFaker.address().citySuffix());
        }

        // Корпус (иногда)
        if (ThreadLocalRandom.current().nextBoolean()) {
            address.setBlock(generateBlock());
        }

        // Квартира (иногда)
        if (ThreadLocalRandom.current().nextBoolean()) {
            address.setApartment(String.valueOf(countryFaker.number().numberBetween(1, 160)));
        }

        return address;
    }

    private String generateBlock() {
        String[] blocks = {"А", "Б", "В", "1", "2"};
        String block = blocks[ThreadLocalRandom.current().nextInt(blocks.length)];
        if (ThreadLocalRandom.current().nextBoolean()) {
            block += ThreadLocalRandom.current().nextInt(1, 5);
        }
        return block;
    }
}

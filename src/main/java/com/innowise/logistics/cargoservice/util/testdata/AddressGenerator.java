package com.innowise.logistics.cargoservice.util.testdata;

import com.innowise.logistics.cargoservice.entity.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressGenerator implements Generator<Address> {

    private final Faker faker = new Faker(new Locale("ru")); // Русская локализация

    @Override
    public Address[] generate(int quantity) {
        log.info("Генерация {} адресов", quantity);
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


    /**
     * Генерирует один случайный адрес.
     */
    private Address generateSingleAddress() {
        Address address = new Address();

        // Страна (чаще всего Беларусь, иногда другие)
        String[] countries = {"Беларусь", "Россия", "Польша", "Литва", "Украина"};
        address.setCountry(countries[ThreadLocalRandom.current().nextInt(countries.length)]);

        // Город
        address.setCity(faker.address().city());

        // Улица
        address.setStreet(faker.address().streetName());

        // Почтовый индекс (формат: 123456 или 123456-7890)
        String zip = faker.address().zipCode();
        // Обрезаем до 10 символов (как в БД)
        address.setZipCode(zip.length() > 10 ? zip.substring(0, 10) : zip);

        // Номер дома (от 1 до 100)
        address.setHouse(faker.number().numberBetween(1, 100));

        // Микрорайон (только если город большой)
        if (ThreadLocalRandom.current().nextBoolean()) {
            address.setMicrodistrict(faker.address().cityPrefix() + " " + faker.address().citySuffix());
        }

        // Корпус (иногда)
        if (ThreadLocalRandom.current().nextBoolean()) {
            address.setBlock(generateBlock());
        }

        // Квартира (иногда)
        if (ThreadLocalRandom.current().nextBoolean()) {
            address.setApartment(String.valueOf(faker.number().numberBetween(1, 160)));
        }

        return address;
    }

    /**
     * Генерирует случайный корпус: буква или буква с номером.
     */
    private String generateBlock() {
        String[] blocks = {"А", "Б", "В", "Г", "Д", "Е", "1", "2", "3"};
        String block = blocks[ThreadLocalRandom.current().nextInt(blocks.length)];

        // Иногда добавляем номер к букве (например, "А1", "Б2")
        if (ThreadLocalRandom.current().nextBoolean()) {
            block += ThreadLocalRandom.current().nextInt(1, 10);
        }

        return block;
    }

    /**
     * Генерирует адрес с заданными параметрами (для тестов).
     */
    public Address generateCustomAddress(String country, String city, String street, int house) {
        Address address = new Address();
        address.setCountry(country);
        address.setCity(city);
        address.setStreet(street);
        address.setHouse(house);
        address.setZipCode(faker.address().zipCode().substring(0, Math.min(10, faker.address().zipCode().length())));
        return address;
    }

    /**
     * Генерирует адрес для конкретной страны.
     */
    public Address[] generateForCountry(int quantity, String country) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество адресов должно быть положительным числом");
        }

        Address[] addresses = new Address[quantity];

        for (int i = 0; i < quantity; i++) {
            Address address = generateSingleAddress();
            address.setCountry(country);
            addresses[i] = address;
        }

        return addresses;
    }
}

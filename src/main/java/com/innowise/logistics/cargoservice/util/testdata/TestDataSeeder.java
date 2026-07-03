package com.innowise.logistics.cargoservice.util.testdata;

import com.innowise.logistics.cargoservice.dto.request.ImageCargoUploadRequest;
import com.innowise.logistics.cargoservice.dto.request.ImageSkuUploadRequest;
import com.innowise.logistics.cargoservice.entity.Address;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Dimension;
import com.innowise.logistics.cargoservice.entity.Location;
import com.innowise.logistics.cargoservice.entity.Sku;
import com.innowise.logistics.cargoservice.mongo.repository.ImageCargoMetadataRepository;
import com.innowise.logistics.cargoservice.mongo.repository.ImageSkuMetadataRepository;
import com.innowise.logistics.cargoservice.mongo.service.ImageCargoServiceImpl;
import com.innowise.logistics.cargoservice.mongo.service.ImageSkuServiceImpl;
import com.innowise.logistics.cargoservice.repository.AddressRepository;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import com.innowise.logistics.cargoservice.repository.DimensionRepository;
import com.innowise.logistics.cargoservice.repository.LocationRepository;
import com.innowise.logistics.cargoservice.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
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

    // Репозитории JPA для сохранения в MongoDB
    private final ImageSkuMetadataRepository imageSkuMetadataRepository;
    private final ImageCargoMetadataRepository imageCargoMetadataRepository;

    private final ImageSkuServiceImpl imageSkuService;
    private final ImageCargoServiceImpl imageCargoService;

    private final GridFsTemplate gridFsTemplate;
    private final MongoTemplate mongoTemplate;

    private final Random random = new Random();

    /**
     * Заполняет базу данных PostgreSQL сбалансированным графом связанных сущностей.
     * Метод полностью безопасен для повторных вызовов.
     * @param imageQuantity желаемое количество физических коробок на складе
     */
    @Transactional
    public void seedAllTestData(int imageQuantity, Boolean isCleanUp) {
        log.info("=== НАЧАЛО СИНХРОННОЙ ГЕНЕРАЦИИ И СОХРАНЕНИЯ ТЕСТОВЫХ ДАННЫХ ===");

        if (isCleanUp) {  // нужно ли предварительно очищать все БД
            cleanDatabases();
        }

        // 1. Генерируем и сохраняем базовые адреса складов
        Address[] rawAddresses = addressGenerator.generate(10);
        List<Address> savedAddresses = addressRepository.saveAll(List.of(rawAddresses));
        log.info("✓ Успешно сохранено адресов складов в DB: {}", savedAddresses.size());

        // 2. Генерируем ячейки хранения на базе сохраненных адресов
        int locationCount = Math.max(20, imageQuantity / 10);
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


                // === теперь сделаем картинки для Sku ===
                String messageSku =
//                        "\nSku.id: " + rawSku.getId() +
                        " SKU: " + rawSku.getName()
//                        "\nSku.description: " + rawSku.getDescription() +
//                        "\nSku.createdAt: " + rawSku.getCreatedAt()
                        ;
//                MultipartFile[] images = generateImages(random.nextInt(10), messageSku);
                MultipartFile[] images = generateImages(ThreadLocalRandom.current().nextInt(1, 10), messageSku);
                log.info("_сгенерировано: {} Sku изображений", images.length);


                ImageSkuUploadRequest imageUploadRequest = ImageSkuUploadRequest.builder()
                        .id(rawSku.getId())
                        .description(rawSku.getDescription())
                        .sortOrder(random.nextInt(3))
                        .isPrimary(random.nextBoolean())
                        .build();
                Arrays.stream(images).forEach(img -> imageSkuService.uploadImage(img, imageUploadRequest));
            }
        }
        log.info("✓ Обработано каталожных артикулов (SKU). Итого в обойме: {}", savedSkus.size());


        // 5. Вызываем конвейер сборки Cargo на базе объектов, уже имеющих первичные ключи PostgreSQL
        Cargo[] rawCargos = cargoGenerator.generateWithSavedEntities(
                imageQuantity,
                savedSkus.toArray(new Sku[0]),
                savedDimensions.toArray(new Dimension[0]),
                savedLocations.toArray(new Location[0])
        );

        List<Cargo> savedCargos = cargoRepository.saveAll(List.of(rawCargos));
        log.info("=== Postgres: УСПЕШНО ЗАВЕРШЕНО. Физических товаров (Cargo) добавлено в DB: {} ===", savedCargos.size());











        for (Cargo cargo : savedCargos) {

                // === теперь сделаем картинки для Cargo ===
                String messageCargo =" Cargo: " + cargo.getName();
                MultipartFile[] images = generateImages(ThreadLocalRandom.current().nextInt(1, 15), messageCargo);
                log.info("_сгенерировано: {} Cargo изображений", images.length);

                // Описание картинки, которое будет храниться в Mongo - метаданных картинки. Чисто информативное назначение.
                StringBuilder cargoDescription = new StringBuilder("Изображение для товара Cargo");
                cargoDescription.append(" с id=").append(cargo.getId());
                cargoDescription.append("; наименование товара: ").append(cargo.getName());
                cargoDescription.append("; категория товара: ").append(cargo.getCategory());
                cargoDescription.append("; артикул товара: ").append(cargo.getSku().getName());
                cargoDescription.append("; (id артикула товара: ").append(cargo.getSku().getId());
                cargoDescription.append("); вес товара=").append(cargo.getWeight());
                cargoDescription.append(" кг.; габариты упаковки товара: длинна=").append(cargo.getDimension().getLength());
                cargoDescription.append(" м., ширина=").append(cargo.getDimension().getWidth());
                cargoDescription.append(" м., высота=").append(cargo.getDimension().getHeight());
                cargoDescription.append(" м.");

                ImageCargoUploadRequest imageUploadRequest = ImageCargoUploadRequest.builder()
                        .id(cargo.getId())
                        .description(cargoDescription.toString())
                        .sortOrder(random.nextInt(3))
                        .isPrimary(random.nextBoolean())
                        .build();
                Arrays.stream(images).forEach(img -> imageCargoService.uploadImage(img, imageUploadRequest));
        }

















        log.info("=== УСПЕШНО ЗАВЕРШЕНО. Физических товаров (Cargo) добавлено в DB: {} ===", savedCargos.size());
    }

    /**
     * Очищает репозитории Postgres и Mongo
     */
    private void cleanDatabases() {
        cargoRepository.deleteAll();
        locationRepository.deleteAll();
        addressRepository.deleteAll();
        skuRepository.deleteAll();
        dimensionRepository.deleteAll();
        log.info("✅ Postgres очищен");

        // Очистка метаданных
        imageSkuMetadataRepository.deleteAll();
        imageCargoMetadataRepository.deleteAll();
        log.info("✅ Метаданные изображений удалены");

        // 2. Удаляем все файлы из GridFS
        gridFsTemplate.delete(new Query(Criteria.where("_id").exists(true)));
        gridFsTemplate.delete(new Query());

//        // ... очистка других коллекций
//        mongoTemplate.dropCollection("fs.files");
//        mongoTemplate.dropCollection("fs.chunks");
//        // Или если бакет другой, то "my_bucket.files" и "my_bucket.chunks"
//        log.info("✅ GridFS коллекции полностью удалены");




        // Проверяем, есть ли коллекции перед удалением
        boolean fsFilesExists = mongoTemplate.collectionExists("fs.files");
        boolean fsChunksExists = mongoTemplate.collectionExists("fs.chunks");
        log.info("До удаления: fs.files существует? {}, fs.chunks существует? {}", fsFilesExists, fsChunksExists);

        // Удаляем коллекции
        try {
            mongoTemplate.dropCollection("fs.files");
            log.info("✅ Коллекция fs.files удалена");
        } catch (Exception e) {
            log.error("❌ Ошибка при удалении fs.files: {}", e.getMessage());
        }

        try {
            mongoTemplate.dropCollection("fs.chunks");
            log.info("✅ Коллекция fs.chunks удалена");
        } catch (Exception e) {
            log.error("❌ Ошибка при удалении fs.chunks: {}", e.getMessage());
        }

        // Проверяем после удаления
        fsFilesExists = mongoTemplate.collectionExists("fs.files");
        fsChunksExists = mongoTemplate.collectionExists("fs.chunks");
        log.info("После удаления: fs.files существует? {}, fs.chunks существует? {}", fsFilesExists, fsChunksExists);




        log.info("✅ Все файлы удалены из GridFS");
    }

    /**
     * Генерирует коллекцию картинок
     */
    private MultipartFile[] generateImages(int quantity, String message) {
        MultipartFile[] images = new MultipartFile[quantity];
        for (int i = 0; i < quantity; i++) {

            // 1. Получаем текущую дату-время с миллисекундами
            LocalDateTime now = LocalDateTime.now();

            // 2. Создаём форматтер с миллисекундами (3 знака)
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ss_SSS");

            // 3. Форматируем в строку
            String formattedDate = now.format(formatter);

            // Вывод в виде: 2026-07-02_15-30-45_123
            // Вывод в виде: 45_123
            String text = formattedDate + " " + message;

            try {
                MultipartFile file = ImageGenerator.generateImage(
                        800, 600,
                        text,                           // это пойдет в отрисовку картинки
                        formattedDate + ".png"          // это будет имя файла
                );
                images[i] = file;
                log.debug("✅ Изображение сгенерировано: " + file.getOriginalFilename() + ", 📏 Размер: " + file.getSize() + " байт");

            } catch (Exception e) {
                log.error("""
                        \nОшибка при генерации изображения № {} для message = {} при попытке сгенрировать {} картинок\n
                        """, i, message, quantity);
                e.printStackTrace();
            }

        }

        return images;
    }
}

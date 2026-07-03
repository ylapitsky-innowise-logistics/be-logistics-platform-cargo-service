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
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    private static final Map<String, String> SKU_TO_IMAGE_MAP = Map.ofEntries(
            Map.entry("Беспроводные наушники", "Беспроводные_наушники.png"),
            Map.entry("Настольная лампа светодиодная", "Настольная_лампа_светодиодная.png"),
            Map.entry("Ноутбук рабочий", "Ноутбук_рабочий.png"),
            Map.entry("Смартфон премиум", "Смартфон_премиум.png"),
            Map.entry("Увлажнитель воздуха ультразвуковой", "Увлажнитель_воздуха_ультразвуковой.png"),
            Map.entry("Умные часы", "Умные_часы.png"),
            Map.entry("Книга Паттерны проектирования Enterprise-приложений", "Книга_Паттерны_проектирования_Enterprise-приложений.png"),
            Map.entry("Книга Руководство по Spring Boot 3", "Книга_Руководство_по_Spring_Boot_3.png"),
            Map.entry("Книга Философия Java", "Книга_Философия_Java.png"),
            Map.entry("Книга Чистая Архитектура", "Книга_Чистая_Архитектура.png"),
            Map.entry("Коврик для йоги нескользящий", "Коврик_для_йоги_нескользящий.png"),
            Map.entry("Набор разборных гантелей", "Набор_разборных_гантелей.png"),
            Map.entry("Спортивный рюкзак водонепроницаемый", "Спортивный_рюкзак_водонепроницаемый.png"),
            Map.entry("Футбольный мяч турнирный", "Футбольный_мяч_турнирный.png"),
            Map.entry("Кресло офисное анатомическое", "Кресло_офисное_анатомическое.png"),
            Map.entry("Стеллаж металлический складской", "Стеллаж_металлический_складской.png"),
            Map.entry("Стол письменный эргономичный", "Стол_письменный_эргономичный.png"),
            Map.entry("Канцелярский набор премиум", "Канцелярский_набор_премиум.png"),
            Map.entry("Компактный складной зонт", "Компактный_складной_зонт.png"),
            Map.entry("Термокружка вакуумная", "Термокружка_вакуумная.png")
    );

    /**
     * Заполняет базу данных PostgreSQL сбалансированным графом связанных сущностей.
     * Метод полностью безопасен для повторных вызовов.
     * @param imageQuantity желаемое количество физических коробок на складе
     */
    @Transactional
    public void seedAllTestData(int imageQuantity, Boolean isCleanUp) {
        log.debug("=== НАЧАЛО СИНХРОННОЙ ГЕНЕРАЦИИ И СОХРАНЕНИЯ ТЕСТОВЫХ ДАННЫХ ===");


        // 🔍 ДИАГНОСТИКА
        checkImageFilesExist();

        if (isCleanUp) {
            cleanDatabases();
        }

        List<Address> savedAddresses = seedAddresses();
        List<Location> savedLocations = seedLocations(imageQuantity, savedAddresses);
        List<Dimension> savedDimensions = seedDimensions();
        List<Sku> savedSkus = seedSkusWithImages();
        List<Cargo> savedCargos = seedCargos(imageQuantity, savedSkus, savedDimensions, savedLocations);

        log.debug("=== УСПЕШНО ЗАВЕРШЕНО. Физических товаров (Cargo) добавлено в DB: {} ===", savedCargos.size());
    }

    // ==================== БЛОКИ СОХРАНЕНИЯ СУЩНОСТЕЙ ====================

    // Генерируем и сохраняем базовые адреса складов
    private List<Address> seedAddresses() {
        Address[] rawAddresses = addressGenerator.generate(10);
        List<Address> savedAddresses = addressRepository.saveAll(List.of(rawAddresses));
        log.debug("✓ Успешно сохранено адресов складов в DB: {}", savedAddresses.size());
        return savedAddresses;
    }

    // Генерируем ячейки хранения на базе сохраненных адресов
    private List<Location> seedLocations(int imageQuantity, List<Address> savedAddresses) {
        int locationCount = Math.max(20, imageQuantity / 10);
        Location[] rawLocations = locationGenerator.generateForAddresses(locationCount, savedAddresses.toArray(new Address[0]));
        List<Location> savedLocations = locationRepository.saveAll(List.of(rawLocations));
        log.debug("✓ Успешно сохранено складских ячеек в DB: {}", savedLocations.size());
        return savedLocations;
    }

    // Генерируем и сохраняем габариты упаковки
    private List<Dimension> seedDimensions() {
        Dimension[] rawDimensions = dimensionGenerator.generate(15);
        List<Dimension> savedDimensions = dimensionRepository.saveAll(List.of(rawDimensions));
        log.debug("✓ Успешно сохранено физических типоразмеров коробок в DB: {}", savedDimensions.size());
        return savedDimensions;
    }

    // Генерируем и сохраняем Sku (20шт.) + изображения (>1 на каждое Sku)
    private List<Sku> seedSkusWithImages() {
        Sku[] rawSkus = skuGenerator.generate(20);
        List<Sku> savedSkus = new ArrayList<>();

        Map<String, Sku> existingSkusMap = skuRepository.findAll().stream()
                .collect(Collectors.toMap(Sku::getName, sku -> sku));

        for (Sku rawSku : rawSkus) {
            if (existingSkusMap.containsKey(rawSku.getName())) {
                savedSkus.add(existingSkusMap.get(rawSku.getName()));
            } else {
                savedSkus.add(skuRepository.save(rawSku));
                uploadImagesForSku(rawSku);
            }
        }

        log.debug("✓ Обработано {} каталожных артикулов (SKU). Итого сейчас в работе: {} SKU",
                rawSkus.length, savedSkus.size());
        return savedSkus;
    }

    private List<Cargo> seedCargos(
            int imageQuantity, List<Sku> savedSkus, List<Dimension> savedDimensions, List<Location> savedLocations) {
        Cargo[] rawCargos = cargoGenerator.generateWithSavedEntities(
                imageQuantity,
                savedSkus.toArray(new Sku[0]),
                savedDimensions.toArray(new Dimension[0]),
                savedLocations.toArray(new Location[0])
        );

        List<Cargo> savedCargos = cargoRepository.saveAll(List.of(rawCargos));
        log.debug("=== Postgres: УСПЕХ. Физических товаров (Cargo) добавлено в DB: {} шт. ===", savedCargos.size());

        savedCargos.forEach(this::uploadImagesForCargo);

        return savedCargos;
    }

    // ==================== ЗАГРУЗКА ИЗОБРАЖЕНИЙ ИЗ ФАЙЛОВ ====================

    /**
     * Загружает изображение для SKU из файла resources/testdata/images/sku/
     * @param skuDescription название SKU
     * @return MultipartFile или null, если файл не найден
     */
    private MultipartFile loadImageForSku(String skuDescription) {
        String imageFileName = SKU_TO_IMAGE_MAP.get(skuDescription);
        if (imageFileName == null) {
            return null;
        }

        try {
            // ✅ ПРОСТОЙ СПОСОБ: прямой путь от корня проекта
            String userDir = System.getProperty("user.dir");
            String fullPath = userDir + "/src/main/resources/testdata/images/sku/" + imageFileName;
            File file = new File(fullPath);

            if (file.exists()) {
                byte[] content = Files.readAllBytes(file.toPath());
                log.info("✅ Загружен файл: {}", fullPath);
                return createMultipartFile(file.getName(), content);
            } else {
                log.warn("⚠️ Файл НЕ НАЙДЕН: {}", fullPath);
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Ошибка загрузки {}: {}", imageFileName, e.getMessage());
            return null;
        }
    }

    /**
     * Создает MultipartFile из байтов
     */
    private MultipartFile createMultipartFile(String filename, byte[] content) {
        return new MultipartFile() {
            @Override
            public String getName() { return filename; }
            @Override
            public String getOriginalFilename() { return filename; }
            @Override
            public String getContentType() { return "image/png"; }
            @Override
            public boolean isEmpty() { return content.length == 0; }
            @Override
            public long getSize() { return content.length; }
            @Override
            public byte[] getBytes() throws IOException { return content; }
            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(content);
            }
            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.write(dest.toPath(), content);
            }
        };
    }

    private void uploadImagesForSku(Sku sku) {
        // 🔥 ПЕРВЫЙ ПРИОРИТЕТ: пробуем загрузить существующее изображение из resources
//        MultipartFile primaryImage = loadImageForSku(sku.getName());
        MultipartFile primaryImage = loadImageForSku(sku.getDescription());

        if (primaryImage != null) {
            // Загружаем основное изображение как PRIMARY
            ImageSkuUploadRequest primaryRequest = ImageSkuUploadRequest.builder()
                    .id(sku.getId())
                    .description("Основное изображение для SKU: " + sku.getName())
                    .sortOrder(0)
                    .isPrimary(true)
                    .build();
            imageSkuService.uploadImage(primaryImage, primaryRequest);
            log.debug("✅ Загружено основное изображение для SKU: {}", sku.getName());

            // Генерируем дополнительные изображения (1-5 штук)
            int extraCount = random.nextInt(1, 6);
            String messageSku = " Доп. изображение для SKU: " + sku.getName();
            MultipartFile[] extraImages = generateImages(extraCount, messageSku);

            // Загружаем дополнительные изображения (НЕ primary)
            for (int i = 0; i < extraImages.length; i++) {
                ImageSkuUploadRequest extraRequest = ImageSkuUploadRequest.builder()
                        .id(sku.getId())
                        .description("Дополнительное изображение #" + (i + 1) + " для SKU: " + sku.getName())
                        .sortOrder(i + 1)  // сортировка после primary (0)
                        .isPrimary(false)
                        .build();
                imageSkuService.uploadImage(extraImages[i], extraRequest);
            }
            log.debug("_сгенерировано дополнительно: {} Sku изображений", extraImages.length);

        } else {
            String messageSku = " SKU: " + sku.getName();
            MultipartFile[] images = generateImages(random.nextInt(1, 12), messageSku);
            log.debug("_сгенерировано: {} Sku изображений", images.length);

            ImageSkuUploadRequest imageUploadRequest = ImageSkuUploadRequest.builder()
                    .id(sku.getId())
                    .description(sku.getDescription())
                    .sortOrder(random.nextInt(3))
                    .isPrimary(random.nextBoolean())
                    .build();

            Arrays.stream(images).forEach(img -> imageSkuService.uploadImage(img, imageUploadRequest));
        }
    }

    // ==================== ЗАГРУЗКА ИЗОБРАЖЕНИЙ ====================

    private void uploadImagesForCargo(Cargo cargo) {
        String messageCargo = " Cargo: " + cargo.getName();
        MultipartFile[] images = generateImages(random.nextInt(1, 15), messageCargo);
        log.debug("_сгенерировано: {} Cargo изображений", images.length);

        ImageCargoUploadRequest imageUploadRequest = ImageCargoUploadRequest.builder()
                .id(cargo.getId())
                .description(buildCargoDescription(cargo))
                .sortOrder(random.nextInt(3))   // порядок сортировки
//                .isPrimary(random.nextBoolean())    // Является - ли передаваемая картинка главной?
                .isPrimary(false)    // Является - ли передаваемая картинка главной?
                .build();

        Arrays.stream(images).forEach(img -> imageCargoService.uploadImage(img, imageUploadRequest));
    }

    private String buildCargoDescription(Cargo cargo) {
        return new StringBuilder("Изображение для товара Cargo")
                .append(" с id=").append(cargo.getId())
                .append("; наименование товара: ").append(cargo.getName())
                .append("; категория товара: ").append(cargo.getCategory())
                .append("; артикул товара: ").append(cargo.getSku().getName())
                .append("; (id артикула товара: ").append(cargo.getSku().getId())
                .append("); вес товара=").append(cargo.getWeight())
                .append("кг.; габариты упаковки товара: длинна=").append(cargo.getDimension().getLength())
                .append("см., ширина=").append(cargo.getDimension().getWidth())
                .append("см., высота=").append(cargo.getDimension().getHeight())
                .append("см.")
                .toString();
    }

    // ==================== ГЕНЕРАЦИЯ ИЗОБРАЖЕНИЙ ====================

    private MultipartFile[] generateImages(int quantity, String message) {
        MultipartFile[] images = new MultipartFile[quantity];
        for (int i = 0; i < quantity; i++) {
            String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ss_SSS"));
            String text = formattedDate + " " + message;

            try {
                MultipartFile file = ImageGenerator.generateImage(
                        800, 600,
                        text,
                        formattedDate + ".png"
                );
                images[i] = file;
                log.debug("✅ Изображение сгенерировано: " + file.getOriginalFilename() + ", 📏 Размер: " + file.getSize() + " байт");
            } catch (Exception e) {
                log.error("Ошибка при генерации изображения № {} для message = {} при попытке сгенерировать {} картинок", i, message, quantity);
                e.printStackTrace();
            }
        }
        return images;
    }

    // ==================== ОЧИСТКА БАЗ ====================

    private void cleanDatabases() {
        cleanPostgres();
        cleanMongoMetadata();
        cleanGridFs();
        log.debug("✅ Все файлы удалены из GridFS");
    }

    private void cleanPostgres() {
        cargoRepository.deleteAll();
        locationRepository.deleteAll();
        addressRepository.deleteAll();
        skuRepository.deleteAll();
        dimensionRepository.deleteAll();
        log.debug("✅ Postgres очищен");
    }

    private void cleanMongoMetadata() {
        imageSkuMetadataRepository.deleteAll();
        imageCargoMetadataRepository.deleteAll();
        log.debug("✅ Метаданные изображений удалены");
    }

    private void cleanGridFs() {
        gridFsTemplate.delete(new Query(Criteria.where("_id").exists(true)));
        gridFsTemplate.delete(new Query());

        boolean fsFilesExists = mongoTemplate.collectionExists("fs.files");
        boolean fsChunksExists = mongoTemplate.collectionExists("fs.chunks");
        log.debug("До удаления: fs.files существует? {}, fs.chunks существует? {}", fsFilesExists, fsChunksExists);

        dropCollectionSafely("fs.files");
        dropCollectionSafely("fs.chunks");

        fsFilesExists = mongoTemplate.collectionExists("fs.files");
        fsChunksExists = mongoTemplate.collectionExists("fs.chunks");
        log.debug("После удаления: fs.files существует? {}, fs.chunks существует? {}", fsFilesExists, fsChunksExists);
    }

    private void dropCollectionSafely(String collectionName) {
        try {
            mongoTemplate.dropCollection(collectionName);
            log.debug("✅ Коллекция {} удалена", collectionName);
        } catch (Exception e) {
            log.error("❌ Ошибка при удалении {}: {}", collectionName, e.getMessage());
        }
    }















    // ==================== ДИАГНОСТИКА ====================

    /**
     * Простая проверка наличия файлов
     */
    private void checkImageFilesExist() {
        log.info("=== ПРОВЕРКА ФАЙЛОВ ===");
        String userDir = System.getProperty("user.dir");
        String fullPath = userDir + "/src/main/resources/testdata/images/sku/";
        File dir = new File(fullPath);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            log.info("✅ Папка найдена: {}", fullPath);
            log.info("📄 Найдено файлов: {}", files != null ? files.length : 0);
            if (files != null && files.length > 0) {
                for (File file : files) {
                    log.info("   - {}", file.getName());
                }
            }
        } else {
            log.error("❌ Папка НЕ НАЙДЕНА: {}", fullPath);
        }
        log.info("=== КОНЕЦ ПРОВЕРКИ ===");
    }
}

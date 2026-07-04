package com.innowise.logistics.cargoservice.controller;

import com.innowise.logistics.cargoservice.util.testdata.TestDataSeeder;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.innowise.logistics.cargoservice.constant.ApiImageConstants.TEST_DATA_GENERATE_URL;

@Slf4j
@RestController
@RequestMapping(TEST_DATA_GENERATE_URL)
@RequiredArgsConstructor
@Validated                                          // Включаем валидацию параметров запроса фреймворком
@Profile({"dev", "test"})                           // 🛑 Жесткий щит безопасности! Эндпоинт физически заблокирован и отсутствует на Production
public class TestDataController {

    private final TestDataSeeder testDataSeeder;    // Наш транзакционный оркестратор

    /**
     * POST /api/v1/test-data/generate
     * Запуск комплексного конвейера генерации и сохранения связанных сущностей в PostgreSQL.
     * Возвращает чистый HTTP-статус 200 OK без тела ответа.
     */
    @PostMapping
    public ResponseEntity<Void> generateTestData(
            @RequestParam(name = "cargos-quantity", required = false, defaultValue = "5")
            @Positive(message = "Количество генерируемых товаров 'Cargo' должно быть целым положительным числом!")
            int cargosQuantity,

            @RequestParam(name = "pre-clean", required = false, defaultValue = "false")
            Boolean isCleanUp) {

        log.info("REST запрос на комплексную генерацию тестовой матрицы данных. Целевое количество Cargo: {}", cargosQuantity);

        // Запускаем конвейер оркестрации и каскадного сохранения в СУБД
        testDataSeeder.seedAllTestData(cargosQuantity, isCleanUp);

        log.info("Генерация и сохранение {} товаров успешно завершены.", cargosQuantity);

        // Возвращаем чистый HTTP статус 200 OK без передачи лишних данных в теле ответа
        return ResponseEntity.ok().build();
    }
}

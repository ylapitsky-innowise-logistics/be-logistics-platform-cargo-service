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

@Slf4j
@RestController
@RequestMapping("/api/v1/catalog/test-data/generate")
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
    @PostMapping("/cargos")
    public ResponseEntity<Void> generateTestData(
            @RequestParam(name = "cargos", required = false, defaultValue = "20")
            @Positive(message = "Количество генерируемых товаров должно быть целым положительным числом")
            int cargosQuantity) {

        log.info("REST запрос на комплексную генерацию тестовой матрицы данных. Целевое количество Cargo: {}", cargosQuantity);

        // Запускаем конвейер оркестрации и каскадного сохранения в СУБД
        testDataSeeder.seedAllTestData(cargosQuantity);

        log.info("Генерация и сохранение {} товаров успешно завершены.", cargosQuantity);

        // Возвращаем чистый HTTP статус 200 OK без передачи лишних данных в теле ответа
        return ResponseEntity.ok().build();
    }
}
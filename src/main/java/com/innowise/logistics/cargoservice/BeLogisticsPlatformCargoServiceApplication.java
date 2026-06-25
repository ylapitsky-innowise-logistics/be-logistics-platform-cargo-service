package com.innowise.logistics.cargoservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing // ⬅️ Без неё Spring Data JPA проигнорирует @CreatedDate
@EnableJpaRepositories(basePackages = "com.innowise.logistics.cargoservice.repository")
@EnableMongoAuditing // 🟢 БЕЗ НЕЁ @CreatedDate в MongoDB работать НЕ БУДЕТ
@EnableMongoRepositories(basePackages = "com.innowise.logistics.cargoservice.mongo.repository") // Если в будущем появятся кастомные интерфейсы для Mongo, складывай их в отдельный пакет:
public class BeLogisticsPlatformCargoServiceApplication {

    @PostConstruct
    public void init() {
        // Намертво устанавливаем часовой пояс МСК для всего приложения JVM
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
    }

	public static void main(String[] args) {
		SpringApplication.run(BeLogisticsPlatformCargoServiceApplication.class, args);
	}
}

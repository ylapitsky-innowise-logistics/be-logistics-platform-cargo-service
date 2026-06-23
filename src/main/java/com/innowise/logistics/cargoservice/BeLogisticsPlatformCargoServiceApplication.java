package com.innowise.logistics.cargoservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaAuditing // ⬅️ Без неё Spring Data JPA проигнорирует @CreatedDate
@EnableJpaRepositories(basePackages = "com.innowise.logistics.cargoservice.repository")
// Если в будущем появятся кастомные интерфейсы для Mongo, складывай их в отдельный пакет:
@EnableMongoRepositories(basePackages = "com.innowise.logistics.cargoservice.mongo.repository")
public class BeLogisticsPlatformCargoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeLogisticsPlatformCargoServiceApplication.class, args);
	}
}

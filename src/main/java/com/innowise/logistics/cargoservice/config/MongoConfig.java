package com.innowise.logistics.cargoservice.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    // Этот бин необходим контроллеру, чтобы открывать потоки скачивания байт напрямую в HTTP-ответ.
    @Bean
    public GridFSBucket gridFSBucket(MongoClient mongoClient) {
        // Извлекаем имя базы данных прямо из настроек подключения в yaml
        return GridFSBuckets.create(mongoClient.getDatabase("cargo_storage_db"));
    }
}

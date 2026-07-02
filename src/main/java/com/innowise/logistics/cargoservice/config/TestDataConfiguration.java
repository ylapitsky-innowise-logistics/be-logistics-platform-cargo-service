package com.innowise.logistics.cargoservice.config;

import com.innowise.logistics.cargoservice.controller.TestDataController;
import com.innowise.logistics.cargoservice.mongo.service.ImageCargoServiceImpl;
import com.innowise.logistics.cargoservice.mongo.service.ImageSkuServiceImpl;
import com.innowise.logistics.cargoservice.repository.*;
import com.innowise.logistics.cargoservice.util.testdata.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "test"}) // 🟢 ОДИН РАЗ намертво закрываем абсолютно всю фабрику тестовых данных от Production
public class TestDataConfiguration {

    @Bean
    public AddressGenerator addressGenerator() {
        return new AddressGenerator();
    }

    @Bean
    public LocationGenerator locationGenerator(AddressGenerator addressGenerator) {
        return new LocationGenerator(addressGenerator);
    }

    @Bean
    public DimensionGenerator dimensionGenerator() {
        return new DimensionGenerator();
    }

    @Bean
    public SkuGenerator skuGenerator() {
        return new SkuGenerator();
    }

    @Bean
    public CargoGenerator cargoGenerator(
            SkuGenerator skuGenerator,
            DimensionGenerator dimensionGenerator,
            LocationGenerator locationGenerator) {
        return new CargoGenerator(skuGenerator, dimensionGenerator, locationGenerator);
    }

    @Bean
    public ImageGenerator imageGenerator() {
        return new ImageGenerator();
    }

    /**
     * ВЫНОСИМ СИДЕР СЮДА.
     * Spring IoC автоматически соберет граф зависимостей (генераторы + репозитории PostgreSQL)
     * и зарегистрирует оркестратор в контексте приложения.
     */
    @Bean
    public TestDataSeeder testDataSeeder(
            AddressGenerator addressGenerator,
            SkuGenerator skuGenerator,
            DimensionGenerator dimensionGenerator,
            LocationGenerator locationGenerator,
            CargoGenerator cargoGenerator,
            AddressRepository addressRepository,
            SkuRepository skuRepository,
            DimensionRepository dimensionRepository,
            LocationRepository locationRepository,
            CargoRepository cargoRepository,
            ImageSkuServiceImpl imageSkuService,
            ImageCargoServiceImpl imageCargoService
            ) {

        return new TestDataSeeder(
                addressGenerator,
                skuGenerator,
                dimensionGenerator,
                locationGenerator,
                cargoGenerator,
                addressRepository,
                skuRepository,
                dimensionRepository,
                locationRepository,
                cargoRepository,
                imageSkuService,
                imageCargoService
        );
    }

    /**
     * Регистрация веб-контроллера как Spring-бина.
     * Зависимость TestDataSeeder внедрится автоматически.
     */
    @Bean
    public TestDataController testDataController(TestDataSeeder testDataSeeder) {
        return new TestDataController(testDataSeeder);
    }
}
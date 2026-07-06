package com.innowise.logistics.cargoservice.constant;

import lombok.experimental.UtilityClass;

@UtilityClass                   // Ломбок сделает класс final, скроет конструктор и запретит наследование
public class Constants {

    // Базовая валюта платформы для финансовых расчетов
    public static final String CURRENCY = "RUB";

    // Здесь же в будущем можно хранить системные лимиты
    public static final int DEFAULT_PAGE_SIZE = 20;
}

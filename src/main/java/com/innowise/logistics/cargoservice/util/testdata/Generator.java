package com.innowise.logistics.cargoservice.util.testdata;

/**
 * Базовый контракт для фабрик генерации тестовых данных.
 */
public interface Generator<T> {

    T[] generate(int quantity);

}

package com.innowise.logistics.cargoservice.util.testdata;

public interface Generator<T> {

    public T[] generate(int quantity);
}

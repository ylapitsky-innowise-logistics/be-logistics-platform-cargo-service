package com.innowise.logistics.cargoservice.util.testdata;

import com.innowise.logistics.cargoservice.entity.Dimension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DimensionGenerator implements Generator<Dimension> {
    @Override
    public Dimension[] generate(int quantity) {
        return null;
    }
}

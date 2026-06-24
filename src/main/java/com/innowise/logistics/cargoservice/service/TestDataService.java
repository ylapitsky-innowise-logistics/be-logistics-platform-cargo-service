package com.innowise.logistics.cargoservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.logistics.cargoservice.dto.request.SkuCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.SkuUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.*;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Sku;
import com.innowise.logistics.cargoservice.entity.Status;
import com.innowise.logistics.cargoservice.mapper.CargoMapper;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import com.innowise.logistics.cargoservice.repository.ReservationRepository;
import com.innowise.logistics.cargoservice.repository.SkuRepository;
import com.innowise.logistics.cargoservice.util.testdata.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 🟢 Глобальная оптимизация чтения на уровне класса
public class TestDataService {

    private final AddressGenerator addressGenerator;
    private final LocationGenerator locationGenerator;
    private final DimensionGenerator dimensionGenerator;
    private final SkuGenerator skuGenerator;
    private final CargoGenerator cargoGenerator;


    private final AddressService addressService;
    private final LocationService locationService;
    private final DimensionService dimensionService;
    private final SkuService skuService;
    private final CargoService cargoService;



    private final CargoRepository cargoRepository;

}

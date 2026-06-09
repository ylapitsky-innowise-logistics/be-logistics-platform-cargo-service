package com.innowise.logistics.cargoservice.service;

import com.innowise.logistics.cargoservice.dto.CargoResponseDto;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.repository.CargoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CargoService {

    private final CargoRepository cargoRepository;

    @Transactional(readOnly = true)
    public Page<CargoResponseDto> getCatalogItems(Pageable pageable) {
        Page<Cargo> cargoPage = cargoRepository.findAll(pageable);

        // Маппим Entity в DTO "на лету"
        return cargoPage.map(this::convertToDto);
    }

    private CargoResponseDto convertToDto(Cargo cargo) {
        String dimensionsStr = String.format("%.1fx%.1fx%.1f",
                cargo.getDimension().getLength(),
                cargo.getDimension().getWidth(),
                cargo.getDimension().getHeight());

        String locationStr = String.format("%s / %s",
                cargo.getLocation().getRack(),
                cargo.getLocation().getShelf() != null ? cargo.getLocation().getShelf() : "Нет полки");

        return new CargoResponseDto(
                cargo.getId(),
                cargo.getSku().getName(),
                cargo.getMongoDocId(),
                cargo.getName(),
                cargo.getCategory(),
                cargo.getWeight(),
                dimensionsStr,
                cargo.getPrice(),
                locationStr,
                cargo.getCreatedAt(),
                cargo.getStatus(),
                cargo.getStatusAt()
        );
    }
}

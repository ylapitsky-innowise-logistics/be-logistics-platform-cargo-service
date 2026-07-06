package com.innowise.logistics.cargoservice.service;

import com.innowise.logistics.cargoservice.dto.request.DimensionCreatingRequest;
import com.innowise.logistics.cargoservice.dto.request.DimensionUpdateRequest;
import com.innowise.logistics.cargoservice.dto.response.DimensionCreatingResponse;
import com.innowise.logistics.cargoservice.dto.response.DimensionViewResponse;
import com.innowise.logistics.cargoservice.entity.Dimension;
import com.innowise.logistics.cargoservice.repository.DimensionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DimensionService {

    private final DimensionRepository dimensionRepository;

    @Transactional
    public DimensionCreatingResponse createDimension(DimensionCreatingRequest request) {
        Dimension dimension = new Dimension();
        dimension.setLength(request.getLength());
        dimension.setWidth(request.getWidth());
        dimension.setHeight(request.getHeight());

        Dimension saved = dimensionRepository.save(dimension);
        return new DimensionCreatingResponse(saved.getId());
    }

    public DimensionViewResponse getDimensionById(Long id) {
        return dimensionRepository.findById(id)
                .map(this::mapToViewResponse)
                .orElseThrow(() -> new EntityNotFoundException("Габариты с ID " + id + " не найдены"));
    }

    public Page<DimensionViewResponse> getAllDimensions(Pageable pageable) {
        return dimensionRepository.findAll(pageable)
                .map(this::mapToViewResponse);
    }

    @Transactional
    public DimensionViewResponse updateDimension(Long id, DimensionUpdateRequest request) {
        Dimension dimension = dimensionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Габариты с ID " + id + " не найдены"));

        dimension.setLength(request.getLength());
        dimension.setWidth(request.getWidth());
        dimension.setHeight(request.getHeight());

        return mapToViewResponse(dimension);
    }

    @Transactional
    public void deleteDimension(Long id) {
        if (!dimensionRepository.existsById(id)) {
            throw new EntityNotFoundException("Габариты с ID " + id + " не найдены");
        }
        dimensionRepository.deleteById(id);
    }

    private DimensionViewResponse mapToViewResponse(Dimension dimension) {
        return new DimensionViewResponse(
                dimension.getId(),
                dimension.getLength(),
                dimension.getWidth(),
                dimension.getHeight()
        );
    }
}

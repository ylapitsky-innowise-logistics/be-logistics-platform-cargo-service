package com.innowise.logistics.cargoservice.mapper;

import com.innowise.logistics.cargoservice.dto.response.CargoViewResponse;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Dimension;
import com.innowise.logistics.cargoservice.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CargoMapper {

    CargoMapper INSTANCE = Mappers.getMapper(CargoMapper.class);

    @Mapping(target = "skuId", source = "sku.id")
    @Mapping(target = "skuName", source = "sku.name")
    @Mapping(target = "dimensions", source = "dimension", qualifiedByName = "formatDimensions")
    @Mapping(target = "location", source = "location", qualifiedByName = "formatLocation")
    CargoViewResponse toDto(Cargo cargo);

    @Named("formatDimensions")
    default String formatDimensions(Dimension dimension) {
        if (dimension == null) {
            return "0x0x0";
        }
        return String.format("%.1fx%.1fx%.1f",
                dimension.getLength(),
                dimension.getWidth(),
                dimension.getHeight());
    }

    @Named("formatLocation")
    default String formatLocation(Location location) {
        if (location == null) {
            return "Неизвестно";
        }
        String shelf = location.getShelf() != null ? location.getShelf() : "Нет полки";
//        String addressStr = String.format("%s, %s, %s %s",
//                location.getAddress().getCountry(),
//                location.getAddress().getCity(),
//                location.getAddress().getStreet(),
//                location.getAddress().getHouse());
        return String.format("%s / %s / %s", location.getRack(), shelf, location.getAddress());
    }
}

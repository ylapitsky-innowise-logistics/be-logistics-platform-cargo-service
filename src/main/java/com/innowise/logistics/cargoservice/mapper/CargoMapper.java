package com.innowise.logistics.cargoservice.mapper;

import com.innowise.logistics.cargoservice.dto.response.CargoViewResponse;
import com.innowise.logistics.cargoservice.entity.Cargo;
import com.innowise.logistics.cargoservice.entity.Dimension;
import com.innowise.logistics.cargoservice.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface CargoMapper {

    CargoMapper INSTANCE = Mappers.getMapper(CargoMapper.class);

    @Mapping(target = "skuId", source = "sku.id")
    @Mapping(target = "skuName", source = "sku.name")
    @Mapping(target = "dimensions", source = "dimension", qualifiedByName = "formatDimensions")
    @Mapping(target = "location", source = "location", qualifiedByName = "formatLocation")
    @Mapping(target = "weight", source = "weight", qualifiedByName = "formatWeight")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "formatInstant")
    @Mapping(target = "statusAt", source = "statusAt", qualifiedByName = "formatInstant")
    CargoViewResponse toDto(Cargo cargo);

    @Named("formatDimensions")
    default String formatDimensions(Dimension dimension) {
        if (dimension == null) {
            return "0x0x0";
        }
        return String.format("%.1f x %.1f x %.1f",
                dimension.getLength(),
                dimension.getWidth(),
                dimension.getHeight());
    }

    @Named("formatLocation")
    default String formatLocation(Location location) {
        if (location == null) {
            return "Неизвестно";
        }
        String shelf = location.getShelf() != null ? "Полка:" + location.getShelf() : "Нет полки";
        String rack = location.getRack() != null ? "Стеллаж:" + location.getShelf() : "Нет стеллажа";
        StringBuilder address = new StringBuilder("Адрес:");
        if (location.getAddress().getCountry() != null) {
            address.append(" country:").append(location.getAddress().getCountry());}
        if (location.getAddress().getZipCode() != null) {
            address.append(", zipCode:").append(location.getAddress().getZipCode());}
        if (location.getAddress().getCity() != null) {
            address.append(", city:").append(location.getAddress().getCity());}
        if (location.getAddress().getMicrodistrict() != null) {
            address.append(", microdistrict:").append(location.getAddress().getMicrodistrict());}
        if (location.getAddress().getStreet() != null) {
            address.append(", street:").append(location.getAddress().getStreet());}
        if (location.getAddress().getHouse() != null) {
            address.append(", house:").append(location.getAddress().getHouse());}
        if (location.getAddress().getBlock() != null) {
            address.append(", block:").append(location.getAddress().getBlock());}
        if (location.getAddress().getApartment() != null) {
            address.append(", apartment:").append(location.getAddress().getApartment());}
        return String.format("%s / %s / %s", rack, shelf, address);
    }


    @Named("formatWeight")
    default String formatWeight(Double weight) {
        if (weight == null) {
            return "0.00";
        }
        return String.format("%.2f", weight);
    }

    @Named("formatInstant")
    default String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss z")
                .withZone(ZoneId.of("GMT+3"));
        return formatter.format(instant);
    }
}

package com.innowise.logistics.cargoservice.dto.response;

public record ImageViewResponse(
        String fileId,
        String fileUrl,
        String fileName,
        String mimeType,
        Long fileSize,
        String description,
        Integer sortOrder,
        Boolean isPrimary
) {}

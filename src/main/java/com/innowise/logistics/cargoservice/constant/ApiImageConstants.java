package com.innowise.logistics.cargoservice.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiImageConstants {

    // Общие для всех изображений (Sku и Cargo)
    public static final String IMAGE_DOWNLOAD_URL = "/{fileId}";
    public static final String IMAGE_GALLERY_URL = "/gallery/{entityId}";
    public static final String IMAGE_PRIMARY_URL = IMAGE_GALLERY_URL + "/primary";

    // для Sku - изображений
    public static final String IMAGE_SKU_BASE_URL = "/api/v1/catalog/images/skus";

    // для Cargo - изображений
    public static final String IMAGE_CARGO_BASE_URL = "/api/v1/catalog/images/cargos";

//    public static final String IMAGE_CARGO_DOWNLOAD_URL =  "/{fileId}";
//    public static final String IMAGE_CARGO_GALLERY_URL =  "/gallery/{entityId}";
//    public static final String IMAGE_CARGO_PRIMARY_URL =  "/gallery/{entityId}/primary";
}

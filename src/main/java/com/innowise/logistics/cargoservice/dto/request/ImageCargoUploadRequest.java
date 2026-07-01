package com.innowise.logistics.cargoservice.dto.request;

import com.innowise.logistics.cargoservice.entity.Category;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ImageCargoUploadRequest {

    @NotNull(message = "ID артикула товара (skuId) является обязательным полем")
    private Long skuId;

    private String skuName = "empty skuName";

    private String cargoName = "cargoName";

    private Category cargoCategory = Category.OTHER;

    private String description = "";
}

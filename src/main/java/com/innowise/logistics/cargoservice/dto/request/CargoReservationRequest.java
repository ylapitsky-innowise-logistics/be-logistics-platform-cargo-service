package com.innowise.logistics.cargoservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CargoReservationRequest {

    /*
    id sku товара, которое мы будем резервировать
     */
    @NotNull(message = "ID артикула товара не может быть пустым")
    Long skuId;

    /*
    Общее количество товаров, которое обязано быть зарезервировано (любыми способами)
     */
    @NotNull(message = "Количество товаров не может быть пустым")
    @Min(value = 1, message = "Количество товаров должно быть не менее 1")
    Integer quantity;

    /*
    Ранжированный список ID товаров (Cargo), которые пользователь хотел бы зарезервировать.
    Если количество товаров в этом списке окажется меньше quantity,
    недостающее количество будет дополнено из пула свободных товаров.
    Если количество товаров в этом списке окажется больше quantity,
    то хвост списка (лишние) будет отброшен.
     */
    List<Long> preferredCargoIds = new ArrayList<>();
}

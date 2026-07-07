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
    Перечень (список, ПРЕОРИТЕЗИРОВАННЫЙ) id товаров, которые хотели-бы купить
    НЕ обязательный параметр.
     */
    List<Integer> reserveCargoIds = new ArrayList<>();
}

/*
По бизнес-логике (следуя ей):
- Если количество товаров reserveCargoIds окажется меньше quantity, то недостающее количество будет дополнено из пула свободных товаров.
- Если количество товаров reserveCargoIds окажется больше quantity, то конец (хвост) просто откинется
 */

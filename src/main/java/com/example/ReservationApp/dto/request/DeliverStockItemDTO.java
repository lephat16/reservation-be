package com.example.ReservationApp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliverStockItemDTO {

    @NotNull(message = "数量は必須です。")
    private Long detailId;
    @NotNull(message = "数量は必須です。")
    private Long warehouseId;
    @NotNull(message = "数量は必須です。")
    @Positive(message = "数量は0より大きくなければなりません。")
    private Integer deliveredQty;
}

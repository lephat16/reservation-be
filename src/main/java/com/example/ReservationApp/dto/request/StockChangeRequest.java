package com.example.ReservationApp.dto.request;

import com.example.ReservationApp.enums.RefType;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockChangeRequest {
    @NotBlank(message = "商品IDは必須です")
    private Long productId;
    @NotBlank(message = "倉庫IDは必須です")
    private Long warehouseId;
    @NotBlank(message = "数量は必須です")
    private int qty;
    private RefType refType;
    private Long refId;
    private String notes;
}

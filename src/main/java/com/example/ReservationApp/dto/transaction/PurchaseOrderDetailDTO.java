package com.example.ReservationApp.dto.transaction;

import java.math.BigDecimal;

import com.example.ReservationApp.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMin;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrderDetailDTO {
    private Long id;
    private Long purchaseOrderId;
    private Long productId;
    private String productName;
    @NotNull(message = "数量は必須です")
    @Positive(message = "数量は0より大きくなければなりません")
    private Integer qty;
    @NotNull(message = "価格は必須です")
    @DecimalMin(value = "0.0", inclusive = false, message = "価格は0より大きくなければなりません")
    private BigDecimal cost; // snapshot price
    private OrderStatus status;
    private String sku;

    private Integer orderedQty;
    private Integer receivedQty;
    private Integer remainingQty;
}

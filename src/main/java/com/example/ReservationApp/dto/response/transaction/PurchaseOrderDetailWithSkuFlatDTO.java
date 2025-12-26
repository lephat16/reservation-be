package com.example.ReservationApp.dto.response.transaction;

import java.math.BigDecimal;

public interface PurchaseOrderDetailWithSkuFlatDTO {
    Long getId();

    Integer getQty();

    BigDecimal getCost();

    Long getProductId();

    Long getPurchaseOrderId();

    String getStatus();

    String getSupplierSku();

    String getProductName();
}

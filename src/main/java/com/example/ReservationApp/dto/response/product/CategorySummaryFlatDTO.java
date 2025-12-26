package com.example.ReservationApp.dto.response.product;

import java.math.BigDecimal;

public interface CategorySummaryFlatDTO {
    String getProductName();

    Long getSupplierId();

    String getSupplierName();

    BigDecimal getPrice();

    Long getQuantity();

    String getWarehouse();
}

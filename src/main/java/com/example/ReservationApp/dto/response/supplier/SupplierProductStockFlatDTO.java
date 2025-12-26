package com.example.ReservationApp.dto.response.supplier;

import java.math.BigDecimal;

public interface SupplierProductStockFlatDTO {
    Long getId();

    String getSku();

    String getProductName();

    String getCategoryName();

    BigDecimal getPrice();

    Integer getTotalQuantity();
}

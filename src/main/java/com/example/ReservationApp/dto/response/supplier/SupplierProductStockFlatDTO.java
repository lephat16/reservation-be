package com.example.ReservationApp.dto.response.supplier;

import java.math.BigDecimal;

public interface SupplierProductStockFlatDTO {
    Long getId();

    String getSku();

    String getProductName();

    String getCategoryName();

    String getSupplierName();

    String getSupplierId();

    BigDecimal getPrice();

    Integer getTotalQuantity();

    Integer getLeadTime();
}

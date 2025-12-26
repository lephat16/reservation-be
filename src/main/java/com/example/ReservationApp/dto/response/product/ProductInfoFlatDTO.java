package com.example.ReservationApp.dto.response.product;

import java.math.BigDecimal;

public interface ProductInfoFlatDTO {
    Long getId();

    String getProductName();

    Long getSupplierId();

    String getSupplierName();

    String getCategoryName();

    String getProductStatus();

    String getProductCode();

    BigDecimal getPrice();

    Long getTotalQuantity();

}

package com.example.ReservationApp.dto.response.supplier;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierProductInCategoryDTO {
    private Long id;
    private String sku;
    private String product;
    private BigDecimal price;
    private Integer stock;
}

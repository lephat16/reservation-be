package com.example.ReservationApp.dto.response.product;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductWithSkuByCategoryDTO {
    private Long supplierProductId;
    private String productName;
    private Long productId;
    private String categoryName;
    private String sku;
    private String unit;
    private String status;
    private BigDecimal  price;
    private Long totalQuantity;
}

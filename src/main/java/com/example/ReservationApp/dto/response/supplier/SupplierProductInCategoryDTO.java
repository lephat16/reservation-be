package com.example.ReservationApp.dto.response.supplier;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplierProductInCategoryDTO {
    private Long id;
    private String sku;
    private String product;
    private String status;
    private BigDecimal price;
    private Integer stock;
    private Integer leadTime;
}

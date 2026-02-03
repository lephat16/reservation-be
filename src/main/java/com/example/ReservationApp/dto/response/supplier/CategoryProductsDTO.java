package com.example.ReservationApp.dto.response.supplier;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryProductsDTO {
    private String categoryName;
    private Long categoryId;
    private String supplierName;
    private Long supplierId;
    
    private List<SupplierProductInCategoryDTO> products;
}

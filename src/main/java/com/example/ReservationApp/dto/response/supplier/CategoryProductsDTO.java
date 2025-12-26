package com.example.ReservationApp.dto.response.supplier;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryProductsDTO {
    private String categoryName;
    private List<SupplierProductInCategoryDTO> products;
}

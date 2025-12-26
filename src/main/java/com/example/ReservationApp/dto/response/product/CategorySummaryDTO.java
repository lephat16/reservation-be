package com.example.ReservationApp.dto.response.product;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategorySummaryDTO {
    private Long categoryId;
    private String categoryName;

    private List<ProductStockDTO> products;
}

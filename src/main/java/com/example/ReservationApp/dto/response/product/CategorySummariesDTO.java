package com.example.ReservationApp.dto.response.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategorySummariesDTO {
    
    private Long id;
    private String categoryName;
    private String products;
    private String suppliers;
    private String status;
}

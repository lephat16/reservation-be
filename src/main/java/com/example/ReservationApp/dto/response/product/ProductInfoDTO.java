package com.example.ReservationApp.dto.response.product;

import java.util.List;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductInfoDTO {
    private Long id;
    private String productName;
    private String code;
    private String categoryName;
    private Long totalStock;
    private String status;
    private List<SupplierPriceDTO> supplier;
}

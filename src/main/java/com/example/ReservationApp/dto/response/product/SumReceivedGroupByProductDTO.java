package com.example.ReservationApp.dto.response.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SumReceivedGroupByProductDTO {
    private Long productId;
    
    private Long receivedQty;

    private String sku;
}

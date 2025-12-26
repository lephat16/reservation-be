package com.example.ReservationApp.dto.response.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockDTO {

    private Long quantity;
    private String warehouse;
}

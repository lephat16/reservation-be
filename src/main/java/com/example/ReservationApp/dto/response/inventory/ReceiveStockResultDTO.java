package com.example.ReservationApp.dto.response.inventory;

import java.util.List;

import com.example.ReservationApp.enums.OrderStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReceiveStockResultDTO {
    
    private Long orderId;
    private OrderStatus status;
    private List<Long> completedDetailIds;     
    private List<StockHistoryDTO> stockHistories;
}

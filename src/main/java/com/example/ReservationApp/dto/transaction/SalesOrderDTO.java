package com.example.ReservationApp.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.ReservationApp.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderDTO {
    private Long id;
    private String customerName;
    private Long userId;
    private String userName;
    private String description;
    private OrderStatus status; // NEW, PROCESSING, COMPLETED, CANCELLED
    private BigDecimal total;
    private LocalDateTime createdAt;

    private List<SalesOrderDetailDTO> details;
}

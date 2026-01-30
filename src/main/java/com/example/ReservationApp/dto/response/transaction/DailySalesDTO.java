package com.example.ReservationApp.dto.response.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailySalesDTO {
    private LocalDate day;
    private BigDecimal dailySales;
    private Long dailyQty;
}

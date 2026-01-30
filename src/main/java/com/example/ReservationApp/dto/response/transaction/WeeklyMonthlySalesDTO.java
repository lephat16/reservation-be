package com.example.ReservationApp.dto.response.transaction;

import java.math.BigDecimal;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeeklyMonthlySalesDTO {

    private Long sodId;
    private Long productId;
    private String month;
    private int week;
    private BigDecimal weeklySales;
    private Long weeklyQty;
    private BigDecimal monthlySales;
    private Long monthlyQty;
}

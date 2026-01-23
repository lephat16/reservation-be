package com.example.ReservationApp.dto.response.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseWithTotalChangedQtyDTO {
    private Long id;
    private String name;
    private String location;
    private Long totalReceivedPo;
    private Long totalDeliveredSo;
    private Long totalReceivedPoInWeek;
    private Long totalDeliveredSoInWeek;
}

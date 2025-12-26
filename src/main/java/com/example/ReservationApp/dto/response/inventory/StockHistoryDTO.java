package com.example.ReservationApp.dto.response.inventory;

import java.time.LocalDateTime;

import com.example.ReservationApp.enums.RefType;
import com.example.ReservationApp.enums.StockChangeType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockHistoryDTO {
    private Long id;
    private Long inventoryStockId;
    private Integer changeQty;
    private StockChangeType type; // IN / OUT / ADJ
    private RefType refType; // PO / SO
    private Long refId;
    private String notes;
    private LocalDateTime createdAt;
}


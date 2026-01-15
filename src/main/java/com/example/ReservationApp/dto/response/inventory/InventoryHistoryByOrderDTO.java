package com.example.ReservationApp.dto.response.inventory;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryHistoryByOrderDTO {
    private Long id;
    private String location;
    private String warehouseName;
    private Integer changeQty;
    private String notes;
    private String productName;
    private String supplierName;
    private String refType;
    private LocalDateTime createdAt;
    private String supplierSku;
    private Long inventoryStockId;
    private String customerName;
}

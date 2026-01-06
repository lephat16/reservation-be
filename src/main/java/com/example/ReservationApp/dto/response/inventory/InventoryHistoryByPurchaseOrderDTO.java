package com.example.ReservationApp.dto.response.inventory;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryHistoryByPurchaseOrderDTO {
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
}

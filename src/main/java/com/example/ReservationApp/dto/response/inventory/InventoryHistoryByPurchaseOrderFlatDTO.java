package com.example.ReservationApp.dto.response.inventory;

import java.time.LocalDateTime;

public interface InventoryHistoryByPurchaseOrderFlatDTO {
    Long getId();

    String getLocation();

    String getWarehouseName();

    Integer getChangeQty();

    String getNotes();

    String getProductName();

    String getSupplierName();

    String getRefType();

    LocalDateTime getCreatedAt();

    String getSupplierSku();
}

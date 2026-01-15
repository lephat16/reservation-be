package com.example.ReservationApp.dto.response.inventory;

import java.time.LocalDateTime;

public interface InventoryHistoryBySaleOrderFlatDTO {
    Long getId();

    String getLocation();

    String getWarehouseName();

    Integer getChangeQty();

    String getNotes();

    String getProductName();

    String getCustomerName();

    String getRefType();

    LocalDateTime getCreatedAt();

    String getSupplierSku();

    Long getInventoryStockId();
}

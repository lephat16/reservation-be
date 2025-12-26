package com.example.ReservationApp.dto.response.transaction;

public interface PurchasesProcessingOrderWithRemainingQtyFlatDTO {
    Long getDetailId();

    Long getProductId();

    String getProductName();

    String getSku();

    Integer getOrderedQty();

    Integer getReceivedQty();

    Integer getRemainingQty();
}

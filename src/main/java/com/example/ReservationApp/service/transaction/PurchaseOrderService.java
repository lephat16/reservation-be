package com.example.ReservationApp.service.transaction;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.transaction.PurchaseOrderDTO;

public interface PurchaseOrderService {

    ResponseDTO<PurchaseOrderDTO> createPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO);

    ResponseDTO<List<PurchaseOrderDTO>> getAllPurchaseOrders();

    ResponseDTO<PurchaseOrderDTO> getPurchaseOrderById(Long purchaseOrderId);

    ResponseDTO<PurchaseOrderDTO> getPurchaseOrderDetailsByPOIdWithSku(Long purchaseOrderId);

    ResponseDTO<PurchaseOrderDTO> updatePurchaseOrder(Long id, PurchaseOrderDTO purchaseOrderDTO);

    ResponseDTO<PurchaseOrderDTO> updatePurchaseOrderQuantityAndDescription(Long id, PurchaseOrderDTO purchaseOrderDTO);

    ResponseDTO<Void> deletePurchaseOrder(Long purchaseOrderId);

    ResponseDTO<PurchaseOrderDTO> placeOrder(Long purchaseOrderId);
}

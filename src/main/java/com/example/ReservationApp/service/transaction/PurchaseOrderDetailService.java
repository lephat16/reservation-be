package com.example.ReservationApp.service.transaction;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.transaction.PurchaseOrderDTO;
import com.example.ReservationApp.dto.transaction.PurchaseOrderDetailDTO;

public interface PurchaseOrderDetailService {

    ResponseDTO<PurchaseOrderDetailDTO> addDetail(Long purchaseOrderId, PurchaseOrderDetailDTO detailDTO);

    ResponseDTO<PurchaseOrderDetailDTO> updateDetail(Long detailId, PurchaseOrderDetailDTO detailDTO);

    ResponseDTO<PurchaseOrderDetailDTO> updateDetailQuantity(Long detailId, int newQty);

    ResponseDTO<Void> deleteDetail(Long detailId);

    ResponseDTO<List<PurchaseOrderDetailDTO>> getByPurchaseOrderId(Long purchaseOrderId);

    ResponseDTO<List<PurchaseOrderDetailDTO>> getByPurchaseOrderWithSku(Long purchaseOrderId);

    List<PurchaseOrderDetailDTO> getDetailEntitysByOrder(Long purchaseOrderId);

    List<PurchaseOrderDetailDTO> getAllDetailEntitys(List<PurchaseOrderDTO> purchaseOrderDTOs);
    
    ResponseDTO<List<PurchaseOrderDetailDTO>> getPurchaseProcessingDetailWithRemainingQty(Long purchaseOrderId);

}

package com.example.ReservationApp.service.transaction;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDetailDTO;

public interface SalesOrderDetailService {
    
    ResponseDTO<SalesOrderDetailDTO> addDetail(Long salesOrderId, SalesOrderDetailDTO detailDTO);

    ResponseDTO<SalesOrderDetailDTO> updateDetail(Long detailId, SalesOrderDetailDTO detailDTO);

    ResponseDTO<Void> deleteDetail(Long detailId);

    ResponseDTO<List<SalesOrderDetailDTO>> getBySalesOrderId(Long salesOrderId);

    List<SalesOrderDetailDTO> getDetailEntitysByOrder(Long salesOrderId);

    List<SalesOrderDetailDTO> getAllDetailEntitys(List<SalesOrderDTO> salesOrderDTOs);
}

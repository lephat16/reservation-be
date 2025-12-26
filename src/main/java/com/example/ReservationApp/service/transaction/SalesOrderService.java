package com.example.ReservationApp.service.transaction;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDTO;

public interface SalesOrderService {
    
    ResponseDTO<SalesOrderDTO> createSalesOrder(SalesOrderDTO SalesOrderDTO);

    ResponseDTO<List<SalesOrderDTO>> getAllSalesOrders();

    ResponseDTO<SalesOrderDTO> getSalesOrderById(Long salesOrderId);

    ResponseDTO<SalesOrderDTO> updateSalesOrder(Long id, SalesOrderDTO salesOrderDTO);

    ResponseDTO<Void> deleteSalesOrder(Long salesOrderId);
}

package com.example.ReservationApp.service.inventory;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.request.DeliverStockItemDTO;
import com.example.ReservationApp.dto.request.ReceiveStockItemDTO;
import com.example.ReservationApp.dto.request.StockChangeRequest;
import com.example.ReservationApp.dto.response.inventory.DeliverStockResultDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryStockDTO;
import com.example.ReservationApp.dto.response.inventory.ReceiveStockResultDTO;

public interface InventoryStockService {

    ResponseDTO<List<InventoryStockDTO>> getAllInventoryStocks();

    ResponseDTO<List<InventoryStockDTO>> getAllStockWithSupplierAndProduct();

    ResponseDTO<InventoryStockDTO> getInventoryStockById(Long invenStockId);

    ResponseDTO<List<InventoryStockDTO>> getInventoryStockByProduct(Long productId);

    ResponseDTO<InventoryStockDTO> increaseStock(StockChangeRequest request);

    ResponseDTO<InventoryStockDTO> decreaseStock(StockChangeRequest request);

    ResponseDTO<InventoryStockDTO> adjustStock(StockChangeRequest request);

    ResponseDTO<ReceiveStockResultDTO> receiveStock(Long poId, List<ReceiveStockItemDTO> receivedItems);

    ResponseDTO<DeliverStockResultDTO> deliverStock(Long poId, List<DeliverStockItemDTO> deliverItems);
    
}

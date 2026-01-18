package com.example.ReservationApp.service.inventory;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryHistoryByOrderDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoriesWithDetailDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoryDTO;

public interface StockHistoryService {

    ResponseDTO<StockHistoryDTO> createStockHistory(StockHistoryDTO stockHistoryDTO, Long inventoryStockId);

    ResponseDTO<List<StockHistoryDTO>> getAllStockHistories();

    ResponseDTO<List<StockHistoriesWithDetailDTO>> getAllStockHistoriesWithDetails();

    ResponseDTO<List<StockHistoryDTO>> getStockHistoryByInventoryId(Long inventoryStockId);

    ResponseDTO<List<StockHistoryDTO>> getStockHistoryByWarehouse(Long stockHistoryId);

    ResponseDTO<List<StockHistoryDTO>> getStockHistoryByProduct(Long productId);

    ResponseDTO<List<StockHistoryDTO>> getRecentStockHistory(LocalDateTime fromDate);
    
    ResponseDTO<List<InventoryHistoryByOrderDTO>> getInventoryHistoryByPurchaseOrder(Long poId);
    
    ResponseDTO<List<InventoryHistoryByOrderDTO>> getInventoryHistoryBySaleOrder(Long soId);
}

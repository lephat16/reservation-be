package com.example.ReservationApp.service.inventory;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoryDTO;

public interface StockHistoryService {

    ResponseDTO<StockHistoryDTO> createStockHistory(StockHistoryDTO stockHistoryDTO, Long inventoryStockId);

    ResponseDTO<List<StockHistoryDTO>> getAllStockHistory();

    ResponseDTO<List<StockHistoryDTO>> getStockHistoryByInventoryId(Long inventoryStockId);

    ResponseDTO<List<StockHistoryDTO>> getStockHistoryByWarehouse(Long stockHistoryId);

    ResponseDTO<List<StockHistoryDTO>> getStockHistoryByProduct(Long productId);

    ResponseDTO<List<StockHistoryDTO>> getRecentStockHistory(LocalDateTime fromDate);
}

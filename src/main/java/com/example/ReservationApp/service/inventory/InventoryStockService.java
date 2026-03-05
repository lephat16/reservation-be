package com.example.ReservationApp.service.inventory;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.request.DeliverStockItemDTO;
import com.example.ReservationApp.dto.request.ReceiveStockItemDTO;
import com.example.ReservationApp.dto.response.inventory.DeliverStockResultDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryStockDTO;
import com.example.ReservationApp.dto.response.inventory.ReceiveStockResultDTO;

public interface InventoryStockService {

    ResponseDTO<List<InventoryStockDTO>> getAllInventoryStocks();

    ResponseDTO<List<InventoryStockDTO>> getAllStockWithSupplierAndProduct();

    ResponseDTO<InventoryStockDTO> getInventoryStockById(Long invenStockId);

    ResponseDTO<List<InventoryStockDTO>> getInventoryStockByProduct(Long productId);

    ResponseDTO<ReceiveStockResultDTO> receiveStock(Long poId, List<ReceiveStockItemDTO> receivedItems);

    ResponseDTO<DeliverStockResultDTO> deliverStock(Long poId, List<DeliverStockItemDTO> deliverItems);

    ResponseDTO<InventoryStockDTO> getBySupplierProductIdAndWarehouseId(Long supplierProductId, Long warehouseId);

    ResponseDTO<List<InventoryStockDTO>> getBySupplierSku(String sku);
    
}

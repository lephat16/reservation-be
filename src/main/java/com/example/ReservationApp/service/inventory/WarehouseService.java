package com.example.ReservationApp.service.inventory;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.inventory.WarehouseDTO;

public interface WarehouseService {

    ResponseDTO<WarehouseDTO> createWarehouse(WarehouseDTO warehouseDTO);

    ResponseDTO<List<WarehouseDTO>> getAllWarehouse();

    ResponseDTO<List<WarehouseDTO>> getAllWarehouseWithLocation();

    ResponseDTO<WarehouseDTO> getWarehouseById(Long warehouseId);

    ResponseDTO<List<WarehouseDTO>> getWarehouseByLocation(String location);

    ResponseDTO<WarehouseDTO> updateWarehouse(Long warehouseId, WarehouseDTO warehouseDTO);

    ResponseDTO<Void> deleteWarehouse(Long warehouseId);
}

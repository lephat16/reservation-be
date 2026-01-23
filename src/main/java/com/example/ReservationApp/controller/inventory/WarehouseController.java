package com.example.ReservationApp.controller.inventory;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.inventory.WarehouseDTO;
import com.example.ReservationApp.dto.response.inventory.WarehouseWithTotalChangedQtyDTO;
import com.example.ReservationApp.service.inventory.WarehouseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Slf4j
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping("/add")
    public ResponseEntity<ResponseDTO<WarehouseDTO>> createWarehouse(@RequestBody WarehouseDTO warehouseDTO) {
        return ResponseEntity.ok(warehouseService.createWarehouse(warehouseDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<WarehouseDTO>>> getAllWarehouse() {
        return ResponseEntity.ok(warehouseService.getAllWarehouse());
    }
    @GetMapping("/with-location/all")
    public ResponseEntity<ResponseDTO<List<WarehouseDTO>>> getAllWarehouseWithLocation() {
        return ResponseEntity.ok(warehouseService.getAllWarehouseWithLocation());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<WarehouseDTO>> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @GetMapping("/location")
    public ResponseEntity<ResponseDTO<List<WarehouseDTO>>> getWarehouseByLocation(@RequestParam("value") String location) {
        return ResponseEntity.ok(warehouseService.getWarehouseByLocation(location));
    }

    @GetMapping("/all-by-sku/with-location/{sku}")
    public ResponseEntity<ResponseDTO<List<WarehouseDTO>>> getWarehouseBySkuWithStocks(@PathVariable String sku) {
        return ResponseEntity.ok(warehouseService.getWarehouseBySkuWithStocks(sku));
    }
    @GetMapping("/all-with-total-changed-qty")
    public ResponseEntity<ResponseDTO<List<WarehouseWithTotalChangedQtyDTO>>> getWarehouseWithTotalChangedQty() {
        return ResponseEntity.ok(warehouseService.getWarehouseWithTotalChangedQty());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO<WarehouseDTO>> updateWarehouse(
            @PathVariable Long id,
            @RequestBody WarehouseDTO warehouseDTO) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, warehouseDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteWarehouse(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.deleteWarehouse(id));
    }
}

package com.example.ReservationApp.controller.inventory;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.request.DeliverStockItemDTO;
import com.example.ReservationApp.dto.request.ReceiveStockItemDTO;
import com.example.ReservationApp.dto.response.inventory.DeliverStockResultDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryStockDTO;
import com.example.ReservationApp.dto.response.inventory.ReceiveStockResultDTO;
import com.example.ReservationApp.service.inventory.InventoryStockService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/inventory/stock")
@RequiredArgsConstructor
@Slf4j
public class InventoryStockController {

    private final InventoryStockService inventoryStockService;

    @PostMapping("/receive-stock/{poId}")
    public ResponseEntity<ResponseDTO<ReceiveStockResultDTO>> receiveStock(
            @PathVariable Long poId,
            @RequestBody List<ReceiveStockItemDTO> receivedItems) {

        return ResponseEntity.ok(inventoryStockService.receiveStock(poId, receivedItems));
    }

    @PostMapping("/deliver-stock/{soId}")
    public ResponseEntity<ResponseDTO<DeliverStockResultDTO>> de(
            @PathVariable Long soId,
            @Valid @RequestBody List<DeliverStockItemDTO> deliverItems) {
        return ResponseEntity.ok(inventoryStockService.deliverStock(soId, deliverItems));
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<InventoryStockDTO>>> getAllInventoryStocks() {

        return ResponseEntity.ok(inventoryStockService.getAllInventoryStocks());
    }

    @GetMapping("/all-with-supplier")
    public ResponseEntity<ResponseDTO<List<InventoryStockDTO>>> getAllStockWithSupplierAndProduct() {

        return ResponseEntity.ok(inventoryStockService.getAllStockWithSupplierAndProduct());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<InventoryStockDTO>> getInventoryStockById(@PathVariable Long id) {

        return ResponseEntity.ok(inventoryStockService.getInventoryStockById(id));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ResponseDTO<List<InventoryStockDTO>>> getInventoryStockByProduct(
            @PathVariable Long productId) {

        return ResponseEntity.ok(inventoryStockService.getInventoryStockByProduct(productId));
    }

    @GetMapping("/by-sp-and-warehouse")
    public ResponseEntity<ResponseDTO<InventoryStockDTO>> getBySupplierProductIdAndWarehouseId(
            @RequestBody Long spId, @RequestBody Long warehouseId) {

        return ResponseEntity.ok(inventoryStockService.getBySupplierProductIdAndWarehouseId(spId, warehouseId));
    }

    @GetMapping("/by-sku/{sku}")
    public ResponseEntity<ResponseDTO<List<InventoryStockDTO>>> getBySupplierSku(
            @PathVariable String sku) {

        return ResponseEntity.ok(inventoryStockService.getBySupplierSku(sku));
    }
}

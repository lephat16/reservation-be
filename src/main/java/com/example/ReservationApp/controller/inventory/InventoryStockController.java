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
import com.example.ReservationApp.dto.request.StockChangeRequest;
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

    @PostMapping("/increase")
    public ResponseEntity<ResponseDTO<InventoryStockDTO>> increaseProduct(@RequestBody StockChangeRequest request) {

        return ResponseEntity.ok(inventoryStockService.increaseStock(request));
    }

    @PostMapping("/decrease")
    public ResponseEntity<ResponseDTO<InventoryStockDTO>> decreaseStock(@RequestBody StockChangeRequest request) {

        return ResponseEntity.ok(inventoryStockService.decreaseStock(request));
    }

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

    @PostMapping("/adjust")
    public ResponseEntity<ResponseDTO<InventoryStockDTO>> adjustStock(@RequestBody StockChangeRequest request) {

        return ResponseEntity.ok(inventoryStockService.adjustStock(request));
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<InventoryStockDTO>>> getAllInventoryStocks() {

        return ResponseEntity.ok(inventoryStockService.getAllInventoryStocks());
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
    
}

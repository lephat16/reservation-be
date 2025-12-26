package com.example.ReservationApp.controller.inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoryDTO;
import com.example.ReservationApp.service.inventory.StockHistoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class StockHistoryController {

    private final StockHistoryService stockHistoryService;

    @PostMapping("/stock-history")
    public ResponseEntity<ResponseDTO<StockHistoryDTO>> createStockHistory(
            @RequestBody StockHistoryDTO stockHistoryDTO,
            @RequestParam Long inventoryStockId) {

        return ResponseEntity.ok(stockHistoryService.createStockHistory(stockHistoryDTO, inventoryStockId));
    }

    @GetMapping("/stock-history/all")
    public ResponseEntity<ResponseDTO<List<StockHistoryDTO>>> getAllStockHistories() {

        return ResponseEntity.ok(stockHistoryService.getAllStockHistory());
    }

    @GetMapping("/stock-history/inventory-stock/{inventoryStockId}")
    public ResponseEntity<ResponseDTO<List<StockHistoryDTO>>> getStockHistoriesByInventoryId(
            @PathVariable Long inventoryStockId) {

        return ResponseEntity.ok(stockHistoryService.getStockHistoryByInventoryId(inventoryStockId));
    }

    @GetMapping("/stock-history/warehouse/{warehouseId}")
    public ResponseEntity<ResponseDTO<List<StockHistoryDTO>>> getStockHistoriesByWarehouse(
            @PathVariable Long warehouseId) {

        return ResponseEntity.ok(stockHistoryService.getStockHistoryByWarehouse(warehouseId));
    }

    @GetMapping("/stock-history/product/{productId}")
    public ResponseEntity<ResponseDTO<List<StockHistoryDTO>>> getStockHistoryByProduct(
            @PathVariable Long productId) {

        return ResponseEntity.ok(stockHistoryService.getStockHistoryByProduct(productId));
    }

    @GetMapping("/stock-history/recent")
    public ResponseEntity<ResponseDTO<List<StockHistoryDTO>>> getRecentStockHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate fromDate) {
        if (fromDate == null) {
            fromDate = LocalDate.now().minusDays(5);
        }
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        return ResponseEntity.ok(stockHistoryService.getRecentStockHistory(fromDateTime));
    }
}

package com.example.ReservationApp.controller.transaction;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDTO;
import com.example.ReservationApp.service.transaction.SalesOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions/")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping("/sales/add")
    ResponseEntity<ResponseDTO<SalesOrderDTO>> createSalesOrder(
            @Valid @RequestBody SalesOrderDTO salesOrderDTO) {
        return ResponseEntity.ok(salesOrderService.createSalesOrder(salesOrderDTO));
    }

    @GetMapping("/sales/all")
    ResponseEntity<ResponseDTO<List<SalesOrderDTO>>> getAllSalesOrders() {
        return ResponseEntity.ok(salesOrderService.getAllSalesOrders());
    }

    @GetMapping("/sales/{soId}")
    ResponseEntity<ResponseDTO<SalesOrderDTO>> getSalesOrderById(@PathVariable Long soId) {
        return ResponseEntity.ok(salesOrderService.getSalesOrderById(soId));
    }

    @PutMapping("/sales/update/{soId}")
    ResponseEntity<ResponseDTO<SalesOrderDTO>> updateSalesOrder(
            @PathVariable Long soId,
            @Valid @RequestBody SalesOrderDTO salesOrderDTO) {
        return ResponseEntity.ok(salesOrderService.updateSalesOrder(soId, salesOrderDTO));
    }

    @PutMapping("/sales/update-qty-and-desc/{soId}")
    ResponseEntity<ResponseDTO<SalesOrderDTO>> updateSalesOrderQuantityAndDescription(
            @PathVariable Long soId,
            @Valid @RequestBody SalesOrderDTO salesOrderDTO) {
        return ResponseEntity.ok(salesOrderService.updateSalesOrderQuantityAndDescription(soId, salesOrderDTO));
    }

    @DeleteMapping("/sales/delete/{soId}")
    ResponseEntity<ResponseDTO<Void>> deleteSalesOrder(@PathVariable Long soId) {
        return ResponseEntity.ok(salesOrderService.deleteSalesOrder(soId));
    }

    @PutMapping("/sales/prepare/{soId}")
    ResponseEntity<ResponseDTO<SalesOrderDTO>> prepareOrder(@PathVariable Long soId) {
        return ResponseEntity.ok(salesOrderService.prepareOrder(soId));
    }
}

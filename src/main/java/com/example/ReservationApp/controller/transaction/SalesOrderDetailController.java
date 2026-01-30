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
import com.example.ReservationApp.dto.response.transaction.WeeklyMonthlySalesDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDetailDTO;
import com.example.ReservationApp.service.transaction.SalesOrderDetailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions/sales-order")
@RequiredArgsConstructor
public class SalesOrderDetailController {

    private final SalesOrderDetailService salesOrderDetailService;

    @PostMapping("/{soId}")
    ResponseEntity<ResponseDTO<SalesOrderDetailDTO>> addDetail(@PathVariable Long soId,
            @Valid @RequestBody SalesOrderDetailDTO poDetailDTO) {
        return ResponseEntity.ok(salesOrderDetailService.addDetail(soId, poDetailDTO));
    }

    @PutMapping("/{detailId}")
    ResponseEntity<ResponseDTO<SalesOrderDetailDTO>> updateDetail(
            @PathVariable Long detailId,
            @Valid @RequestBody SalesOrderDetailDTO poDetailDTO) {
        return ResponseEntity.ok(salesOrderDetailService.updateDetail(detailId, poDetailDTO));
    }

    @DeleteMapping("/{detailId}")
    public ResponseEntity<ResponseDTO<Void>> deleteDetail(@PathVariable Long detailId) {
        return ResponseEntity.ok(salesOrderDetailService.deleteDetail(detailId));
    }

    @GetMapping("/{soId}/details")
    public ResponseEntity<ResponseDTO<List<SalesOrderDetailDTO>>> getByPurchaseOrderId(
            @PathVariable Long soId) {
        return ResponseEntity.ok(salesOrderDetailService.getBySalesOrderId(soId));
    }

    @GetMapping("/{productId}/weekly-sales")
    public ResponseEntity<ResponseDTO<List<WeeklyMonthlySalesDTO>>> getWeeklySalesByProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(salesOrderDetailService.getWeeklySalesByProduct(productId));
    }
}

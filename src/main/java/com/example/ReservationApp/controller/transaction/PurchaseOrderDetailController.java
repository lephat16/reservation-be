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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.transaction.PurchaseOrderDetailDTO;
import com.example.ReservationApp.service.transaction.PurchaseOrderDetailService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions/purchase-order")
@RequiredArgsConstructor
public class PurchaseOrderDetailController {

    private final PurchaseOrderDetailService purchaseOrderDetailService;

    @PostMapping("/{poId}")
    ResponseEntity<ResponseDTO<PurchaseOrderDetailDTO>> addDetail(@PathVariable Long poId,
            @Valid @RequestBody PurchaseOrderDetailDTO poDetailDTO) {
        return ResponseEntity.ok(purchaseOrderDetailService.addDetail(poId, poDetailDTO));
    }

    @PutMapping("/{detailId}")
    ResponseEntity<ResponseDTO<PurchaseOrderDetailDTO>> updateDetail(
            @PathVariable Long detailId,
            @Valid @RequestBody PurchaseOrderDetailDTO poDetailDTO) {
        return ResponseEntity.ok(purchaseOrderDetailService.updateDetail(detailId, poDetailDTO));
    }

    @PutMapping("/qty/{detailId}")
    ResponseEntity<ResponseDTO<PurchaseOrderDetailDTO>> updateDetailQuantity(
            @PathVariable Long detailId,
            @RequestParam @Min(1) int newQty ) {
        return ResponseEntity.ok(purchaseOrderDetailService.updateDetailQuantity(detailId, newQty));
    }

    @DeleteMapping("/{detailId}")
    public ResponseEntity<ResponseDTO<Void>> deleteDetail(@PathVariable Long detailId) {
        return ResponseEntity.ok(purchaseOrderDetailService.deleteDetail(detailId));
    }

    @GetMapping("/{poId}/details")
    public ResponseEntity<ResponseDTO<List<PurchaseOrderDetailDTO>>> getByPurchaseOrderWithSku(
            @PathVariable Long poId) {
        return ResponseEntity.ok(purchaseOrderDetailService.getByPurchaseOrderWithSku(poId));
    }

    @GetMapping("/processing/{poId}/details")
    public ResponseEntity<ResponseDTO<List<PurchaseOrderDetailDTO>>> getPurchaseProcessingDetailWithRemainingQty(
            @PathVariable Long poId) {
        return ResponseEntity.ok(purchaseOrderDetailService.getPurchaseProcessingDetailWithRemainingQty(poId));
    }
}

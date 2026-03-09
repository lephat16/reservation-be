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
import com.example.ReservationApp.dto.transaction.PurchaseOrderDTO;
import com.example.ReservationApp.service.transaction.PurchaseOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions/")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping("/add-purchase")
    ResponseEntity<ResponseDTO<PurchaseOrderDTO>> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        return ResponseEntity.ok(purchaseOrderService.createPurchaseOrder(purchaseOrderDTO));
    }

    @GetMapping("/all-purchases")
    ResponseEntity<ResponseDTO<List<PurchaseOrderDTO>>> getAllPurchasesOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    @GetMapping("/purchase/{supplierId}/get-po-by-supplier")
    ResponseEntity<ResponseDTO<List<PurchaseOrderDTO>>> getPurchaseOrderBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderBySupplier(supplierId));
    }

    @GetMapping("/{poId}/by-purchase")
    ResponseEntity<ResponseDTO<PurchaseOrderDTO>> getPurchaseOrderDetailsByPOIdWithSku(@PathVariable Long poId) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderDetailsByPOIdWithSku(poId));
    }

    @PutMapping("/purchase/update/{poId}")
    ResponseEntity<ResponseDTO<PurchaseOrderDTO>> updatePurchaseOrder(
            @PathVariable Long poId,
            @Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        return ResponseEntity.ok(purchaseOrderService.updatePurchaseOrder(poId, purchaseOrderDTO));
    }

    @PutMapping("/purchase/{poId}/update-qty-and-desc")
    ResponseEntity<ResponseDTO<PurchaseOrderDTO>> updatePurchaseOrderQuantityAndDescription(
            @PathVariable Long poId,
            @Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        return ResponseEntity
                .ok(purchaseOrderService.updatePurchaseOrderQuantityAndDescription(poId, purchaseOrderDTO));
    }

    @DeleteMapping("/purchase/{poId}/delete-po")
    ResponseEntity<ResponseDTO<Void>> deletePurchaseOrder(@PathVariable Long poId) {
        return ResponseEntity.ok(purchaseOrderService.deletePurchaseOrder(poId));
    }

    @PutMapping("/purchase/{poId}/place")
    ResponseEntity<ResponseDTO<PurchaseOrderDTO>> placeOrder(@PathVariable Long poId) {
        return ResponseEntity.ok(purchaseOrderService.placeOrder(poId));
    }
}

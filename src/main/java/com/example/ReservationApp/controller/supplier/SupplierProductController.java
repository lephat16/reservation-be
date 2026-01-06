package com.example.ReservationApp.controller.supplier;

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
import com.example.ReservationApp.dto.response.supplier.CategoryProductsDTO;
import com.example.ReservationApp.dto.supplier.SupplierProductDTO;
import com.example.ReservationApp.dto.supplier.SupplierProductPriceHistoryDTO;
import com.example.ReservationApp.service.supplier.SupplierProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sup-product")
public class SupplierProductController {

        private final SupplierProductService supplierProductService;

        @PostMapping("/add")
        public ResponseEntity<ResponseDTO<SupplierProductDTO>> addSupplierProduct(
                        @RequestBody SupplierProductDTO supplierProductDTO) {
                System.out.println(supplierProductDTO);
                return ResponseEntity
                                .ok(supplierProductService
                                                .createSupplierProduct(supplierProductDTO.getSupplierId(),
                                                                supplierProductDTO.getProductId(),
                                                                supplierProductDTO));

        }

        @GetMapping("/{supplierId}")
        public ResponseEntity<ResponseDTO<List<SupplierProductDTO>>> getProductBySupplier(
                        @PathVariable Long supplierId) {
                return ResponseEntity
                                .ok(supplierProductService.getProductsBySupplier(supplierId));
        }

        @PutMapping("/{spId}")
        public ResponseEntity<ResponseDTO<SupplierProductDTO>> updateSupplierProduct(
                        @PathVariable Long spId,
                        @RequestBody SupplierProductDTO spDTO) {
                return ResponseEntity
                                .ok(supplierProductService.updateSupplierProduct(spId, spDTO));
        }

        @GetMapping("/price-histories")
        public ResponseEntity<ResponseDTO<List<SupplierProductPriceHistoryDTO>>> getPriceHistory() {
                return ResponseEntity
                                .ok(supplierProductService.getPriceHistory());
        }

        @DeleteMapping("{spId}")
        public ResponseEntity<ResponseDTO<Void>> deleteSupplierProduct(@PathVariable Long spId) {
                return ResponseEntity
                                .ok(supplierProductService.deleteSupplierProduct(spId));
        }

        @GetMapping("/with-stock/{supplierId}")
        public ResponseEntity<ResponseDTO<List<CategoryProductsDTO>>> getSupplierProductsWithStock(
                        @PathVariable Long supplierId) {
                return ResponseEntity
                                .ok(supplierProductService.getSupplierProductsWithStock(supplierId));
        }
        @GetMapping("/with-lead-time/{supplierId}")
        public ResponseEntity<ResponseDTO<List<CategoryProductsDTO>>> getSupplierProductsWithLeadTime(
                        @PathVariable Long supplierId) {
                return ResponseEntity
                                .ok(supplierProductService.getSupplierProductsWithLeadTime(supplierId));
        }

}

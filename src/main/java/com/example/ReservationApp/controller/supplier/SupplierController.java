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
import com.example.ReservationApp.dto.supplier.SupplierDTO;
import com.example.ReservationApp.service.supplier.SupplierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 仕入先（サプライヤー）管理用のRESTコントローラー。
 * 
 * このコントローラーは、仕入先の追加、取得、更新、削除
 * CRUD操作を提供。
 * 各エンドポイントはSupplierServiceを利用してビジネスロジックを実行。
 */
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Slf4j
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping("/add")
    public ResponseEntity<ResponseDTO<SupplierDTO>> addSupplier(@RequestBody @Valid SupplierDTO supplierDTO) {

        return ResponseEntity.ok(supplierService.addSupplier(supplierDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<SupplierDTO>>> getAllSuppliers() {

        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<SupplierDTO>> getSupplierById(@PathVariable Long id) {

        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO<SupplierDTO>> updateSupplier(@PathVariable Long id,
            @RequestBody @Valid SupplierDTO supplierDTO) {

        return ResponseEntity.ok(supplierService.updateSupplier(id, supplierDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteSupplier(@PathVariable Long id) {

        return ResponseEntity.ok(supplierService.deleteSupplier(id));
    }
}

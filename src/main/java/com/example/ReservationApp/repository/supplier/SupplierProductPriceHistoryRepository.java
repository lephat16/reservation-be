package com.example.ReservationApp.repository.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.ReservationApp.entity.supplier.SupplierProductPriceHistory;

import jakarta.transaction.Transactional;

public interface SupplierProductPriceHistoryRepository extends JpaRepository<SupplierProductPriceHistory, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM SupplierProductPriceHistory h WHERE h.supplierProduct.id = :supplierProductId")
    void deleteBySupplierProductId(Long supplierProductId);
}

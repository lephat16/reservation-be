package com.example.ReservationApp.repository.supplier;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ReservationApp.entity.supplier.SupplierProductPriceHistory;

public interface SupplierProductPriceHistoryRepository extends JpaRepository<SupplierProductPriceHistory, Long>{
    
}

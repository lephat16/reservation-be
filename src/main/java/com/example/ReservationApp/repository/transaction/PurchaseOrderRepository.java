package com.example.ReservationApp.repository.transaction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ReservationApp.entity.transaction.PurchaseOrder;


public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.createdBy JOIN FETCH po.supplier")
    List<PurchaseOrder> findAllWithUserAndSupplier();

    @Query("""
                SELECT DISTINCT po FROM PurchaseOrder po
                LEFT JOIN FETCH po.createdBy
                LEFT JOIN FETCH po.supplier
                LEFT JOIN FETCH po.details d
                LEFT JOIN FETCH d.product
            """)
    List<PurchaseOrder> findAllWithDetailsUserAndSupplier();

    @Query("""
                SELECT po FROM PurchaseOrder po
                JOIN FETCH po.createdBy
                JOIN FETCH po.supplier
                LEFT JOIN FETCH po.details d
                LEFT JOIN FETCH d.product p
                WHERE po.id = :id
            """)
    Optional<PurchaseOrder> findByIdWithDetails(@Param("id") Long id);

    
    List<PurchaseOrder> findBySupplierId(Long supplierId);

}

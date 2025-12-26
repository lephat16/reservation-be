package com.example.ReservationApp.repository.supplier;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ReservationApp.entity.supplier.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
        boolean existsByName(String name);

        boolean existsByContactInfo(String contactInfo);

        @Query("SELECT s FROM Supplier s " +
                        "LEFT JOIN FETCH s.supplierProducts sp " +
                        "LEFT JOIN FETCH sp.product p " +
                        "LEFT JOIN FETCH p.category c")
        List<Supplier> findAllWithProducts();

        @Query("""
                        SELECT DISTINCT s FROM Supplier s
                        LEFT JOIN FETCH s.supplierProducts sp
                        LEFT JOIN FETCH sp.product p
                        LEFT JOIN FETCH p.category c
                        WHERE s.id = :id
                        """)
        Optional<Supplier> findSupplierWithProductsAndCategory(@Param("id") Long id);
}

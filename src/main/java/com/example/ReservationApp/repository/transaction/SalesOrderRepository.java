package com.example.ReservationApp.repository.transaction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ReservationApp.entity.transaction.SalesOrder;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @Query("""
                SELECT DISTINCT so FROM SalesOrder so
                LEFT JOIN FETCH so.createdBy
                LEFT JOIN FETCH so.details d
                LEFT JOIN FETCH d.product
            """)
    List<SalesOrder> findAllWithDetailsAndUser();

    @Query("""
                SELECT so FROM SalesOrder so
                JOIN FETCH so.createdBy
                LEFT JOIN FETCH so.details d
                LEFT JOIN FETCH d.product p
                WHERE so.id = :id
            """)
    Optional<SalesOrder> findByIdWithDetails(@Param("id") Long id);
}

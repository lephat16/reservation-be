package com.example.ReservationApp.repository.transaction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.example.ReservationApp.entity.transaction.SalesOrder;
import com.example.ReservationApp.entity.transaction.SalesOrderDetail;

public interface SalesOrderDetailRepository extends JpaRepository<SalesOrderDetail, Long> {

    List<SalesOrderDetail> findBySalesOrder(SalesOrder salesOrder);


    @Query("SELECT d FROM SalesOrderDetail d WHERE d.salesOrder.id IN :ids")
    List<SalesOrderDetail> findBySalesOrderIdIn(@Param("ids") List<Long> salesOrderIds);

    Optional<SalesOrderDetail> findBySalesOrderIdAndProductId(Long poId, Long productId);

    Optional<SalesOrderDetail> findBySalesOrderIdAndSupplierProductId(Long poId, Long productId);
}

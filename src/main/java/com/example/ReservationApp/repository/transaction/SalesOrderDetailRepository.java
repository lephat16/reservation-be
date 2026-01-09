package com.example.ReservationApp.repository.transaction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ReservationApp.dto.response.transaction.PurchaseOrderDetailWithSkuFlatDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDetailDTO;
import com.example.ReservationApp.entity.transaction.SalesOrder;
import com.example.ReservationApp.entity.transaction.SalesOrderDetail;

public interface SalesOrderDetailRepository extends JpaRepository<SalesOrderDetail, Long>{
    
    List<SalesOrderDetail> findBySalesOrder(SalesOrder salesOrder);

    // @Query("""
    //             SELECT
    //                 sod.id AS id,
    //                 sod.product.id AS productId,
    //                 sod.salesOrder.id AS salesOrderId,
    //                 p.name AS productName,
    //                 sod.qty AS qty,
    //                 sod.deliveredQty AS deliveredQty,
    //                 sod.price AS price,
    //                 sod.status AS status,
    //                 sp.supplierSku AS sku
    //             FROM SalesOrderDetail sod
    //             JOIN sod.salesOrder so
    //             JOIN so.supplier s
    //             JOIN sod.product p
    //             LEFT JOIN s.supplierProducts sp
    //             WHERE po.id = :poId AND (sp.product = sod.product OR sp IS NULL)
    //         """)
    // List<SalesOrderDetailDTO> findDetailsWithSupplierSku(@Param("poId") Long poId);

    @Query("SELECT d FROM SalesOrderDetail d WHERE d.salesOrder.id IN :ids")
    List<SalesOrderDetail> findBySalesOrderIdIn(@Param("ids") List<Long> salesOrderIds);

    Optional<SalesOrderDetail> findBySalesOrderIdAndProductId(Long poId, Long productId);
}

package com.example.ReservationApp.repository.transaction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ReservationApp.dto.response.transaction.PurchaseOrderDetailWithSkuFlatDTO;
import com.example.ReservationApp.dto.response.transaction.PurchasesProcessingOrderWithRemainingQtyFlatDTO;
import com.example.ReservationApp.entity.transaction.PurchaseOrder;
import com.example.ReservationApp.entity.transaction.PurchaseOrderDetail;

public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Long> {

    List<PurchaseOrderDetail> findByPurchaseOrder(PurchaseOrder purchaseOrder);

    // List<PurchaseOrderDetail> findByPurchaseOrderIdIn(List<Long>
    // purchaseOrderIds);

    @Query("""
                SELECT d FROM PurchaseOrderDetail d
                JOIN FETCH d.product
                WHERE d.purchaseOrder = :po
            """)
    List<PurchaseOrderDetail> findByPurchaseOrderFetchProduct(
            @Param("po") PurchaseOrder po);

    @Query("SELECT d FROM PurchaseOrderDetail d WHERE d.purchaseOrder.id IN :ids")
    List<PurchaseOrderDetail> findByPurchaseOrderIdIn(@Param("ids") List<Long> purchaseOrderIds);

    Optional<PurchaseOrderDetail> findByPurchaseOrderIdAndProductId(Long poId, Long productId);

    @Query("""
                SELECT
                    pod.id AS id,
                    pod.qty AS qty,
                    pod.cost AS cost,
                    pod.product.id AS productId,
                    pod.purchaseOrder.id AS purchaseOrderId,
                    pod.status AS status,
                    sp.supplierSku AS supplierSku,
                    p.name AS productName
                FROM PurchaseOrderDetail pod
                JOIN pod.purchaseOrder po
                JOIN po.supplier s
                JOIN pod.product p
                LEFT JOIN s.supplierProducts sp
                WHERE po.id = :poId AND (sp.product = pod.product OR sp IS NULL)
            """)
    List<PurchaseOrderDetailWithSkuFlatDTO> findDetailsWithSupplierSku(@Param("poId") Long poId);

    @Query(value = """
                SELECT
                    pod.id            AS detailId,
                    p.id              AS productId,
                    p.name            AS productName,
                    sp.supplier_sku   AS sku,
                    pod.qty           AS orderedQty,
                    COALESCE(SUM(sh.change_qty), 0) AS receivedQty,
                    pod.qty - COALESCE(SUM(sh.change_qty), 0) AS remainingQty
                FROM purchase_order_details pod
                JOIN products p ON pod.product_id = p.id
                JOIN purchase_orders po ON pod.purchase_order_id = po.id
                JOIN supplier_products sp
                       ON sp.product_id = p.id
                      AND sp.supplier_id = po.supplier_id
                LEFT JOIN inventory_stocks is_ ON is_.product_id = p.id
                LEFT JOIN stock_histories sh
                       ON sh.inventory_stock_id = is_.id
                      AND sh.ref_type = 'PO'
                      AND sh.ref_id = :poId
                WHERE pod.purchase_order_id = :poId
                GROUP BY pod.id, p.id, p.name, sp.supplier_sku, pod.qty
                ORDER BY pod.id
            """, nativeQuery = true)
    List<PurchasesProcessingOrderWithRemainingQtyFlatDTO> findProcessingDetailWithRemaingQty(@Param("poId") Long poId);

}

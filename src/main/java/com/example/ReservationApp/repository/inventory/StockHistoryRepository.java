package com.example.ReservationApp.repository.inventory;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ReservationApp.dto.response.inventory.InventoryHistoryByPurchaseOrderFlatDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryHistoryBySaleOrderFlatDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoriesWithDetailDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoryDTO;
import com.example.ReservationApp.entity.inventory.StockHistory;

import io.lettuce.core.dynamic.annotation.Param;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {

        @Query("""
                        SELECT
                                sh.inventoryStock.product.id,
                                COALESCE(SUM(sh.changeQty), 0),
                                sp.supplierSku
                        FROM StockHistory sh
                        JOIN sh.inventoryStock s
                        JOIN s.product p
                        JOIN SupplierProduct sp ON sp.product.id = p.id
                        WHERE sh.refType = 'PO'
                        AND sh.refId = :poId
                        GROUP BY sh.inventoryStock.product.id, sp.supplierSku
                        """)
        List<Object[]> sumReceivedQtyByPoGroupByProduct(@Param("poId") Long poId);

        @Query("""
                        SELECT sh.inventoryStock.product.id, COALESCE(SUM(sh.changeQty), 0)
                        FROM StockHistory sh
                        WHERE sh.refType = 'SO'
                        AND sh.refId = :soId
                        GROUP BY sh.inventoryStock.product.id
                        """)
        List<Object[]> sumDeliveredQtyBySoGroupByProduct(@Param("soId") Long soId);

        @Query("""
                        SELECT sh
                        FROM StockHistory sh
                        JOIN FETCH sh.inventoryStock s
                        JOIN FETCH s.product
                        JOIN FETCH s.warehouse

                        """)
        List<StockHistory> findAllWithStockProductWarehouse();

        @Query("""
                        SELECT sh
                        FROM StockHistory sh
                        JOIN FETCH sh.inventoryStock s
                        JOIN FETCH s.product
                        JOIN FETCH s.warehouse
                        WHERE s.id = :inventoryStockId
                        """)
        List<StockHistory> findByInventoryStock(Long inventoryStockId);

        @Query("""
                        SELECT sh
                        FROM StockHistory sh
                        JOIN FETCH sh.inventoryStock s
                        JOIN FETCH s.product
                        JOIN FETCH s.warehouse w
                        WHERE w.id = :warehouseId
                        """)
        List<StockHistory> findByWarehouse(Long warehouseId);

        @Query("""
                        SELECT sh
                        FROM StockHistory sh
                        JOIN FETCH sh.inventoryStock s
                        JOIN FETCH s.product p
                        JOIN FETCH s.warehouse
                        WHERE p.id = :productId
                        """)
        List<StockHistory> findByProduct(Long productId);

        @Query(value = """
                        SELECT
                            sh.created_at AS date,
                            sh.type,
                            sh.change_qty as changeQty
                        FROM stock_histories sh
                        JOIN  inventory_stocks ivs ON ivs.id = sh.inventory_stock_id
                        JOIN  products p ON ivs.product_id = p.id
                        WHERE p.id = :productId
                                    """, nativeQuery = true)
        List<Object[]> findHistoryWithQuantiyAndTypeByProductId(@Param("productId") Long productId);

        @Query("""
                        SELECT sh
                        FROM StockHistory sh
                        JOIN FETCH sh.inventoryStock s
                        JOIN FETCH s.product p
                        JOIN FETCH s.warehouse
                        WHERE sh.createdAt >= :fromDate
                        ORDER BY sh.createdAt DESC
                        """)
        List<StockHistory> findRecentStockHistory(@Param("fromDate") LocalDateTime fromDate);

        @Query(value = """
                        SELECT
                                sh.id,
                                wh.location,
                                wh.name AS warehouse_name,
                                sh.change_qty,
                                sh.notes,
                                p.name AS product_name,
                                s.name AS supplier_name,
                                sh.ref_type,
                                sh.created_at,
                                sp.supplier_sku,
                                ivs.id AS inventory_stock_id
                        FROM stock_histories sh
                        JOIN inventory_stocks ivs ON sh.inventory_stock_id = ivs.id
                        JOIN warehouses wh ON wh.id = ivs.warehouse_id
                        JOIN products p ON p.id = ivs.product_id
                        JOIN purchase_orders po ON po.id = sh.ref_id
                        JOIN suppliers s ON s.id = po.supplier_id
                        JOIN supplier_products sp
                                ON sp.product_id = ivs.product_id
                                AND sp.supplier_id = po.supplier_id
                        WHERE po.id = :poId
                        ORDER BY sh.created_at DESC;

                                                """, nativeQuery = true)
        List<InventoryHistoryByPurchaseOrderFlatDTO> findInventoryHistoryByPurchaseOrder(@Param("poId") Long poId);

        @Query(value = """
                        SELECT
                                sh.id,
                                wh.location,
                                wh.name AS warehouse_name,
                                sh.change_qty,
                                sh.notes,
                                p.name AS product_name,
                                so.customer_name,
                                sh.ref_type,
                                sh.created_at,
                                sp.supplier_sku,
                                ivs.id AS inventory_stock_id
                        FROM stock_histories sh
                        JOIN inventory_stocks ivs ON sh.inventory_stock_id = ivs.id
                        JOIN warehouses wh ON wh.id = ivs.warehouse_id
                        JOIN sales_orders so ON so.id = sh.ref_id
                        JOIN supplier_products sp ON sp.id = ivs.supplier_product_id
                        JOIN products p ON p.id = sp.product_id
                        WHERE sh.ref_id = :soId AND sh.ref_type = 'SO'
                        ORDER BY sh.created_at DESC;

                        """, nativeQuery = true)
        List<InventoryHistoryBySaleOrderFlatDTO> findInventoryHistoryBySaleOrder(@Param("soId") Long soId);

        @Query(value = """
                        SELECT
                                sh.id,
                                sh.inventory_stock_id,
                                sh.change_qty,
                                sh.type,
                                sh.ref_type,
                                sh.ref_id,
                                sh.notes,
                                sh.created_at,
                        	sp.supplier_sku,
                                p.name AS productName,
                                p.unit,
                        	wh.name AS warehouse_name,
                        	COALESCE(so_user.name, po_user.name) AS user_name,
                         	COALESCE(sod.price, pod.cost) AS price,
                         	COALESCE(s.name, so.customer_name) AS participant_name

                        FROM public.stock_histories sh
                        JOIN inventory_stocks ins ON ins.id=sh.inventory_stock_id
                        JOIN supplier_products sp ON sp.id=ins.supplier_product_id
                        JOIN products p ON p.id=sp.product_id
                        JOIN warehouses wh ON ins.warehouse_id=wh.id
                        LEFT JOIN sales_orders so ON sh.ref_id=so.id
                        LEFT JOIN users so_user ON so_user.id=so.user_id
                        LEFT JOIN sales_order_details sod ON sod.sales_order_id=so.id
                        LEFT JOIN purchase_orders po ON sh.ref_id=po.id
                        LEFT JOIN users po_user ON po_user.id=po.user_id
                        LEFT JOIN purchase_order_details pod ON pod.purchase_order_id=po.id
                        LEFT JOIN suppliers s ON po.supplier_id=s.id
                        ORDER BY id ASC
                                    """, nativeQuery = true)
        List<StockHistoriesWithDetailDTO> findAllStockHistoriesWithDetails();

}

package com.example.ReservationApp.repository.inventory;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ReservationApp.entity.inventory.StockHistory;

import io.lettuce.core.dynamic.annotation.Param;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {

        // @Query("""
        // SELECT COALESCE(SUM(sh.changeQty),0)
        // FROM StockHistory sh
        // WHERE sh.refType = 'PO'
        // AND sh.refId = :poId
        // AND sh.inventoryStock.product.id = :productId
        // """)

        // Integer sumReceivedQtyByPoAndProduct(
        // @Param("poId") Long poId,
        // @Param("productId") Long productId);

        @Query("""
                            SELECT sh.inventoryStock.product.id, COALESCE(SUM(sh.changeQty), 0)
                            FROM StockHistory sh
                            WHERE sh.refType = 'PO'
                              AND sh.refId = :poId
                            GROUP BY sh.inventoryStock.product.id
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
}

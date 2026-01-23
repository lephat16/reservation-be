package com.example.ReservationApp.repository.inventory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.ReservationApp.entity.inventory.InventoryStock;

import io.lettuce.core.dynamic.annotation.Param;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {

        Optional<InventoryStock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

        @Query("""
                            SELECT DISTINCT s FROM InventoryStock s
                            LEFT JOIN FETCH s.product
                            LEFT JOIN FETCH s.warehouse
                            LEFT JOIN FETCH s.stockHistories
                        """)
        List<InventoryStock> findAllWithRelations();

        @Query("""
                            SELECT s FROM InventoryStock s
                            LEFT JOIN FETCH s.product
                            LEFT JOIN FETCH s.warehouse
                            LEFT JOIN FETCH s.stockHistories
                            WHERE s.id = :id
                        """)
        Optional<InventoryStock> findByIdWithRelations(@Param("id") Long id);

        List<InventoryStock> findByProductId(Long productId);

        List<InventoryStock> findByProductIdIn(List<Long> productIds);

        List<InventoryStock> findBySupplierProduct_SupplierSku(String sku);

        List<InventoryStock> findBySupplierProduct_SupplierSkuIn(List<String> skus);

        @Query("""
                        SELECT COALESCE(SUM(is.quantity - is.reservedQuantity), 0)
                        FROM InventoryStock is
                        WHERE is.product.id = :productId
                        """)
        int getAvailableStock(Long productId);

        @Query("""
                        SELECT COALESCE(SUM(is.quantity - is.reservedQuantity), 0)
                        FROM InventoryStock is
                        WHERE is.supplierProduct.supplierSku = :sku
                        """)
        int getAvailableStockBySku(@Param("sku") String sku);

        @Modifying
        @Query("""
                        UPDATE InventoryStock is
                        SET is.reservedQuantity = is.reservedQuantity + :qty
                        WHERE is.product.id = :productId
                        """)
        void reserveStock(Long productId, int qty);

        @Modifying
        @Query("""
                        UPDATE InventoryStock is
                        SET is.reservedQuantity = is.reservedQuantity + :qty
                        WHERE is.supplierProduct.supplierSku = :sku
                        """)
        void reserveStockBySku(@Param("sku") String sku, @Param("qty") int qty);

        @Query("""
                        SELECT s
                        FROM InventoryStock s
                        JOIN FETCH s.warehouse
                        WHERE s.product.id
                        IN :productIds
                        """)
        List<InventoryStock> findByProductIdInWithWarehouse(@Param("productIds") List<Long> productIds);

        @Query("""
                        SELECT s FROM InventoryStock s
                        JOIN FETCH s.warehouse
                        WHERE s.id
                        IN :ids
                        """)
        List<InventoryStock> findAllByIdsWithWarehouse(@Param("ids") List<Long> ids);

        @Query(value = """
                        SELECT
                            ivs.quantity,
                            w.name
                        FROM inventory_stocks ivs
                        JOIN  warehouses w ON w.id = ivs.warehouse_id
                        JOIN  products p ON ivs.product_id = p.id
                        WHERE p.id =  :productId
                                """, nativeQuery = true)
        List<Object[]> findStockWithWarehouseAndQtyByProductId(@Param("productId") Long productId);

         @Query("""
                        SELECT s FROM InventoryStock s
                        JOIN FETCH s.product p
                        JOIN FETCH p.category
                        JOIN FETCH s.supplierProduct sp
                        JOIN FETCH sp.supplier
                        JOIN FETCH s.warehouse
                        """)
        List<InventoryStock> findAllStockWithSupplierAndProduct();
}

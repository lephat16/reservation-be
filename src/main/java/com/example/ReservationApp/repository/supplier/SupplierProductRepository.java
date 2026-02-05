package com.example.ReservationApp.repository.supplier;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ReservationApp.dto.response.supplier.SupplierProductStockFlatDTO;
import com.example.ReservationApp.entity.supplier.SupplierProduct;
import com.example.ReservationApp.enums.SupplierProductStatus;

import io.lettuce.core.dynamic.annotation.Param;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Long> {

    Optional <SupplierProduct> findByProductIdAndSupplierId(Long productId, Long SupplierId);

    List<SupplierProduct> findBySupplierId(Long supplierId);

    List<SupplierProduct> findByProductId(Long productId);

    List<SupplierProduct> findAllBySupplierSkuIn(List <String> skus);

    Optional<SupplierProduct> findBySupplierSku(String sku);

    List<SupplierProduct> findBySupplierIdAndStatus(Long supplierId, SupplierProductStatus status);

    boolean existsBySupplierIdAndSupplierSku(Long supplierId, String sku);

    boolean existsBySupplierIdAndSupplierSkuAndIdNot(Long supplierId, String sku, Long excludeId);

    @Query(value = """
                SELECT
                    s.id AS supplierId,
                    s.name AS supplierName,
                    sp.supplier_sku as sku,
                    sp.current_price
                FROM supplier_products sp
                JOIN  suppliers s ON sp.supplier_id = s.id
                JOIN  products p ON sp.product_id = p.id
                WHERE p.id = :productId
            """, nativeQuery = true)
    List<Object[]> getSupplierAndPriceByProductId(@Param("productId") Long productId);

    @Query(value = """
            SELECT
                sp.id,
                sp.supplier_sku AS sku,
                p.name AS productName,
                sp.status AS status,
                c.name AS categoryName,
                c.id AS categoryId,
                sp.current_price AS price,
                sp.lead_time AS leadTime,
                s.name AS supplierName,
                s.id AS supplierId,
                COALESCE(SUM(i.quantity), 0) AS totalQuantity
            FROM supplier_products sp
            JOIN products p ON sp.product_id = p.id
            JOIN categories c ON c.id = p.category_id
            JOIN suppliers s ON s.id = sp.supplier_id
            LEFT JOIN inventory_stocks i ON i.product_id = p.id
            WHERE sp.supplier_id = :supplierId
            GROUP BY sp.id, sp.supplier_sku, p.name, sp.current_price, c.name, s.name, s.id, sp.lead_time, sp.status, c.id
            ORDER BY sp.id
            """, nativeQuery = true)
    List<SupplierProductStockFlatDTO> findSupplierProductsWithStock(@Param("supplierId") Long supplierId);

}

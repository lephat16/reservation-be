package com.example.ReservationApp.repository.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ReservationApp.dto.response.product.ProductInfoFlatDTO;
import com.example.ReservationApp.dto.response.product.ProductWithSkuByCategoryDTO;
import com.example.ReservationApp.entity.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByProductCode(String code);

    boolean existsByName(String name);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByCategoryName(String name);

    @Query(value = """
            SELECT
                p.id,
                p.name,
                p.product_code AS productCode,
                p.description,
                p.unit,
                p.status,
                c.name AS categoryName
            FROM products p
            JOIN categories c ON p.category_id = c.id
            WHERE p.id = :productId;
                   """, nativeQuery = true)
    List<Object[]> findProductWithCatName(@Param("productId") Long id);

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);

    @Query(value = """
               SELECT
                   p.name AS productName,
                   p.product_code AS productCode,
                   p.status AS productStatus,
                   c.name AS categoryName,
                   s.name AS supplierName,
                   s.id AS supplierId,
                   sp.current_price AS price,
                   COALESCE(SUM(i.quantity), 0) AS totalQuantity
               FROM products p
               JOIN categories c ON p.category_id = c.id
               LEFT JOIN supplier_products sp ON sp.product_id = p.id
               INNER JOIN suppliers s ON s.id = sp.supplier_id
               LEFT JOIN inventory_stocks i ON i.product_id = p.id
               LEFT JOIN warehouses w ON w.id = i.warehouse_id
               WHERE p.id = :productId
               GROUP BY
                 p.name,
                 p.product_code,
                 c.name,
                 p.status,
                 s.id,
                 sp.id
               ORDER BY p.name, s.name
            """, nativeQuery = true)
    List<ProductInfoFlatDTO> getProductWithSupplierAndStockById(@Param("productId") Long productId);

    @Query(value = """
               SELECT
                    p.id,
                    p.name AS productName,
                    p.product_code AS productCode,
                    p.status AS productStatus,
                    c.name AS categoryName,
                    s.name AS supplierName,
                    s.id AS supplierId,
                    sp.current_price AS price,
                   COALESCE(SUM(i.quantity), 0) AS totalQuantity
               FROM products p
               JOIN categories c ON p.category_id = c.id
               LEFT JOIN supplier_products sp ON sp.product_id = p.id
               INNER JOIN suppliers s ON s.id = sp.supplier_id
               LEFT JOIN inventory_stocks i ON i.product_id = p.id
               LEFT JOIN warehouses w ON w.id = i.warehouse_id
               GROUP BY
                  p.id,
                  p.name,
                  p.product_code,
                  c.name,
                  p.status,
                  s.id,
                  sp.id
               ORDER BY p.name, s.name
            """, nativeQuery = true)
    List<ProductInfoFlatDTO> getAllProductWithSupplierAndStock();

    @Query(value = """
                SELECT
                    sp.id AS supplier_product_id,
                    p.name AS product_name,
                    p.id AS product_id,
                    c.name AS category_name,
                    sp.supplier_sku AS sku,
                    p.unit,
                    p.status,
					sp.current_price AS price,
					COALESCE(SUM(ins.quantity), 0) AS totalQuantity,
					COALESCE(SUM(ins.reserved_quantity), 0) AS totalReservedQuantity
                FROM products p
                JOIN categories c ON c.id = p.category_id
                JOIN supplier_products sp ON p.id = sp.product_id
                JOIN inventory_stocks ins ON ins.supplier_product_id = sp.id
                WHERE c.id = :categoryId
                GROUP BY sp.id, p.id, p.name, c.name, p.unit, p.status
            """, nativeQuery = true)
    List<ProductWithSkuByCategoryDTO> findAllSupllierProductWithSkuByCategory(@Param("categoryId") Long categoryId);
}

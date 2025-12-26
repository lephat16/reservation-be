package com.example.ReservationApp.repository.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ReservationApp.dto.response.product.CategorySummariesDTO;
import com.example.ReservationApp.dto.response.product.CategorySummaryFlatDTO;
import com.example.ReservationApp.entity.product.Category;
import com.example.ReservationApp.enums.CategoryStatus;

import io.lettuce.core.dynamic.annotation.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByname(String name);

    Optional<Category> findByName(String name);

    List<Category> findByStatus(CategoryStatus status);

    @Query(value = """
                SELECT
                     p.name AS productName,
                    s.id AS supplierId,
                    s.name AS supplierName,
                    sp.current_price AS price,
                    i.quantity AS quantity,
                    w.name AS warehouse
                FROM categories c
                JOIN products p ON p.category_id = c.id
                LEFT JOIN supplier_products sp ON sp.product_id = p.id
                INNER JOIN suppliers s ON s.id = sp.supplier_id
                LEFT JOIN inventory_stocks i ON i.product_id = p.id
                LEFT JOIN warehouses w ON w.id = i.warehouse_id
                WHERE c.id = :categoryId
                ORDER BY p.name, s.name
                                """, nativeQuery = true)
    List<CategorySummaryFlatDTO> getCategorySummaryById(@Param("categoryId") Long categoryId);

    @Query(value = """
                SELECT
                    c.id,
                    c.name AS category_name,
                    STRING_AGG(DISTINCT p.name, ', ') AS products,
                    STRING_AGG(DISTINCT s.name, ', ') AS suppliers,
                    c.status
                FROM categories  c
                LEFT JOIN products p ON p.category_id = c.id
                LEFT JOIN supplier_products sp ON sp.product_id = p.id
                LEFT JOIN suppliers s ON s.id = sp.supplier_id
                GROUP BY c.id, c.name, c.status
                ORDER BY c.id
            """, nativeQuery = true)
    List<CategorySummariesDTO> getAllCategorySummary();

    
}

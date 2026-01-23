package com.example.ReservationApp.repository.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ReservationApp.dto.response.product.CategoryInventorySalesOverviewDTO;
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
                c.status,
                c.description,
                c.image_url,
                c.created_at,
                c.updated_at
            FROM categories  c
            LEFT JOIN products p ON p.category_id = c.id
            LEFT JOIN supplier_products sp ON sp.product_id = p.id
            LEFT JOIN suppliers s ON s.id = sp.supplier_id
            GROUP BY c.id, c.name, c.status
            ORDER BY c.id
                   """, nativeQuery = true)
    List<CategorySummariesDTO> getAllCategorySummary();

    @Query(value = """
            SELECT
                c.id AS category_id,
                c.name AS category_name,
                COUNT(DISTINCT p.id) AS product_count,  -- カテゴリ内のユニークな製品の数をカウント
                ROUND(100.0 * COUNT(DISTINCT p.id) / total_products.total_product, 2) AS percentage,  -- 製品数の合計に対する割合を計算
                COUNT(DISTINCT s.id) AS supplier_count,  -- ユニークな仕入先の数をカウント
                STRING_AGG(DISTINCT s.name, ', ') AS supplier_names,  -- 仕入先名をカンマ区切りで結合
                COALESCE(SUM(ins.quantity), 0) AS total_quantity,  -- 在庫にある製品の合計数量
                COALESCE(sales_info.total_price, 0) AS total_price  -- 販売された製品の合計金額（注文金額の合計）
            FROM public.categories c
            JOIN products p ON c.id = p.category_id

            LEFT JOIN supplier_products sp ON sp.product_id = p.id
            LEFT JOIN suppliers s ON s.id = sp.supplier_id
            -- 製品の合計数を取得するサブクエリ
            CROSS JOIN (SELECT COUNT(*) AS total_product FROM products) total_products
            LEFT JOIN inventory_stocks ins ON ins.supplier_product_id = sp.id  -- inventory_stocksテーブルと結合して製品の在庫数量を取得

             -- 販売額の合計を計算するサブクエリ
            LEFT JOIN (
                SELECT
                    c.id AS category_id,
                    COALESCE(SUM(sod.price), 0) AS total_price
                FROM public.categories c
                JOIN products p ON c.id = p.category_id
                JOIN sales_order_details sod ON sod.product_id = p.id
                WHERE c.id = 1  -- 「Electronics」カテゴリにフィルタリング
                GROUP BY c.id
            ) sales_info ON c.id = sales_info.category_id

            WHERE c.id = :categoryId
            GROUP BY c.id, c.name, total_products.total_product, sales_info.total_price
            ORDER BY c.id ASC
                        """, nativeQuery = true)
    CategoryInventorySalesOverviewDTO findCategorySalesAndInventoryOverviewById(@Param("categoryId") Long categoryId);

}

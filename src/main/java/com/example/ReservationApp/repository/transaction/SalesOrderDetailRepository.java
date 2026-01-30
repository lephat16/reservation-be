package com.example.ReservationApp.repository.transaction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ReservationApp.dto.response.transaction.DailySalesDTO;
import com.example.ReservationApp.dto.response.transaction.WeeklyMonthlySalesDTO;
import com.example.ReservationApp.entity.transaction.SalesOrder;
import com.example.ReservationApp.entity.transaction.SalesOrderDetail;

public interface SalesOrderDetailRepository extends JpaRepository<SalesOrderDetail, Long> {

    List<SalesOrderDetail> findBySalesOrder(SalesOrder salesOrder);

    @Query("SELECT d FROM SalesOrderDetail d WHERE d.salesOrder.id IN :ids")
    List<SalesOrderDetail> findBySalesOrderIdIn(@Param("ids") List<Long> salesOrderIds);

    Optional<SalesOrderDetail> findBySalesOrderIdAndProductId(Long poId, Long productId);

    Optional<SalesOrderDetail> findBySalesOrderIdAndSupplierProductId(Long poId, Long productId);

    @Query(value = """
            SELECT
                sod.id AS sod_id,
	            sod.product_id,
                TO_CHAR(so.created_at, 'YYYY-MM') AS month,
                
                CEIL(
                    EXTRACT(DAY FROM so.created_at) * 4.0
                    / EXTRACT(DAY FROM (DATE_TRUNC('month', so.created_at) + INTERVAL '1 month' - INTERVAL '1 day'))
                )::int AS week,

                -- Weekly totals
                SUM(sod.price * sod.qty) AS weekly_sales,
                SUM(sod.qty) AS weekly_qty,

                -- Monthly totals (window function)
                SUM(SUM(sod.price * sod.qty))
                    OVER (PARTITION BY TO_CHAR(so.created_at, 'YYYY-MM')) AS monthly_sales,
                SUM(SUM(sod.qty))
                    OVER (PARTITION BY TO_CHAR(so.created_at, 'YYYY-MM'))::bigint AS monthly_qty
            FROM sales_order_details sod
            JOIN sales_orders so ON so.id = sod.sales_order_id
            WHERE sod.status = 'COMPLETED'
              AND sod.product_id = :productId
            GROUP BY month, week, sod_id
            ORDER BY month, week
            """, nativeQuery = true)
    List<WeeklyMonthlySalesDTO> findWeeklySalesByProduct(Long productId);

    @Query(value = """
        WITH last_10_days AS (
            SELECT generate_series(
                CURRENT_DATE - INTERVAL '9 days', 
                CURRENT_DATE, 
                '1 day'::interval
            )::date AS day
        )
        SELECT
            d.day,
            COALESCE(SUM(sod.price * sod.qty), 0) AS daily_sales,
            COALESCE(SUM(sod.qty), 0) AS daily_qty
        FROM last_10_days d
        LEFT JOIN sales_order_details sod
            ON sod.sales_order_id IN (
                SELECT so.id
                FROM sales_orders so
                WHERE DATE(so.created_at) = d.day
            )
            AND sod.status = 'COMPLETED'
            AND sod.product_id = :productId
        GROUP BY d.day
        ORDER BY d.day ASC
        """, nativeQuery = true)
    List<DailySalesDTO> findDailySalesLast10Days(@Param("productId") Long productId);
}

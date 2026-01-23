package com.example.ReservationApp.repository.inventory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ReservationApp.dto.response.inventory.WarehouseDTO;
import com.example.ReservationApp.dto.response.inventory.WarehouseWithTotalChangedQtyDTO;
import com.example.ReservationApp.entity.inventory.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
        List<Warehouse> findByLocationContainingIgnoreCase(String location);

        @Query("""
                        SELECT w
                        FROM Warehouse w
                        LEFT JOIN FETCH w.inventoryStocks s
                        LEFT JOIN FETCH s.product
                            WHERE w.id = :id
                        """)
        Optional<Warehouse> findByIdWithStocks(Long id);

        @Query("""
                        SELECT w
                        FROM Warehouse w
                        LEFT JOIN FETCH w.inventoryStocks s
                        LEFT JOIN FETCH s.product
                        """)
        List<Warehouse> findAllWithStocks();

        @Query("""
                        SELECT w
                        FROM Warehouse w
                        LEFT JOIN FETCH w.inventoryStocks s
                        LEFT JOIN FETCH s.product p
                        JOIN FETCH s.supplierProduct sp
                        WHERE sp.supplierSku = :sku
                        """)
        List<Warehouse> findAllBySkuWithStocks(@Param("sku") String sku);

        @Query("""
                            SELECT new com.example.ReservationApp.dto.response.inventory.WarehouseDTO(w.id, w.name, w.location)
                            FROM Warehouse w
                            WHERE w.status = com.example.ReservationApp.enums.WarehouseStatus.ACTIVE
                        """)
        List<WarehouseDTO> findActiveWarehousesWithLocation();

        @Query(value = """
                        SELECT
                            wh.id,
                            wh.name,
                            wh.location,
                            SUM(CASE WHEN sh.ref_type = 'PO' THEN sh.change_qty ELSE 0 END) AS total_received_po,
                            SUM(CASE WHEN sh.ref_type = 'SO' THEN sh.change_qty ELSE 0 END) AS total_delivered_so,
                            SUM(CASE WHEN sh.ref_type = 'PO' AND sh.created_at >= date_trunc('week', NOW())
                                THEN sh.change_qty ELSE 0 END) AS total_received_po_in_week,
                            SUM(CASE WHEN sh.ref_type = 'SO' AND sh.created_at >= date_trunc('week', NOW())
                                THEN sh.change_qty ELSE 0 END) AS total_delivered_so_in_week
                        FROM public.warehouses wh
                        JOIN inventory_stocks ins ON ins.warehouse_id = wh.id
                        JOIN stock_histories sh ON sh.inventory_stock_id = ins.id
                        GROUP BY wh.id, wh.name, wh.location
                        ORDER BY wh.id
                        """, nativeQuery = true)
        List<WarehouseWithTotalChangedQtyDTO> findWarehouseWithTotalChangedQty();
}

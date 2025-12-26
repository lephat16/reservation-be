package com.example.ReservationApp.repository.inventory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ReservationApp.dto.response.inventory.WarehouseDTO;
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
    List<Warehouse> findBAllWithStocks();

    @Query("""
                SELECT new com.example.ReservationApp.dto.response.inventory.WarehouseDTO(w.id, w.name, w.location)
                FROM Warehouse w
                WHERE w.status = com.example.ReservationApp.enums.WarehouseStatus.ACTIVE
            """)
    List<WarehouseDTO> findActiveWarehousesWithLocation();

}

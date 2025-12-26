package com.example.ReservationApp.entity.inventory;

import java.time.LocalDateTime;

import com.example.ReservationApp.enums.RefType;
import com.example.ReservationApp.enums.StockChangeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inventory_stock_id")
    private InventoryStock inventoryStock;

    private Integer changeQty;

    @Enumerated(EnumType.STRING)
    private StockChangeType type; // IN/OUT/ADJ

    @Enumerated(EnumType.STRING)
    private RefType refType; // PO / SO
    private Long refId;
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

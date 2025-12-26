package com.example.ReservationApp.entity.supplier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "supplier_product_price_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierProductPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_product_id", nullable = false)
    private SupplierProduct supplierProduct;

    private BigDecimal price;

    @Column(nullable = false)
    private LocalDate effectiveDate;
    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.effectiveDate == null) {
            this.effectiveDate = LocalDate.now();
        }
        this.createdAt = LocalDateTime.now();
    }
}

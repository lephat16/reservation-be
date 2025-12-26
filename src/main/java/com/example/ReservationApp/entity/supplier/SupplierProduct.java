package com.example.ReservationApp.entity.supplier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.enums.SupplierProductStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "supplier_products", uniqueConstraints = @UniqueConstraint(columnNames = { "supplier_id",
        "supplier_sku" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "supplier_sku", nullable = false, unique = true)
    private String supplierSku;
    private BigDecimal currentPrice;
    private Integer leadTime; // days

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SupplierProductStatus status = SupplierProductStatus.ACTIVE;

    @OneToMany(mappedBy = "supplierProduct")
    @Builder.Default
    private List<SupplierProductPriceHistory> priceHistories = new ArrayList<>();
}

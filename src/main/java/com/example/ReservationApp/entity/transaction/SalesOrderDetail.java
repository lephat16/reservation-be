package com.example.ReservationApp.entity.transaction;

import java.math.BigDecimal;

import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.entity.supplier.SupplierProduct;
import com.example.ReservationApp.enums.OrderStatus;

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
@Table(name = "sales_order_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "supplier_product_id")
    private SupplierProduct supplierProduct;
    

    @Column(nullable = false)
    @Builder.Default
    private Integer deliveredQty = 0;

    private Integer qty;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = OrderStatus.NEW;
        }
        if (deliveredQty == null) {
            deliveredQty = 0;
        }
    }
}

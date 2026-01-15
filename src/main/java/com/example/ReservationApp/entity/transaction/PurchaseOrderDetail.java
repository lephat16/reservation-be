package com.example.ReservationApp.entity.transaction;

import java.math.BigDecimal;

import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.enums.OrderStatus;

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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_order_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @NotNull(message = "数量は必須です")
    @Positive(message = "数量は0より大きくなければなりません")
    private Integer qty;

    @NotNull(message = "価格は必須です")
    @DecimalMin(value = "0.0", inclusive = false, message = "価格は0より大きくなければなりません")
    private BigDecimal cost; // snapshot price

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = OrderStatus.NEW;
        }
    }
}

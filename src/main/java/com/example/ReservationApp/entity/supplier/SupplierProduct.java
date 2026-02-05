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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @JoinColumn(name = "supplier_id", nullable = false)
    @NotNull(message = "Supplierは必須です")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Productは必須です")
    private Product product;

    @Column(name = "supplier_sku", nullable = false, unique = true)
    @NotBlank(message = "SKUは必須です")
    @Size(min = 3, max = 20, message = "SKUは3文字以上、20文字以下である必要があります")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "SKUは英大文字、数字、ハイフンのみ使用可能です")
    private String supplierSku;

    @NotNull(message = "現在価格は必須です")
    @DecimalMin(value = "0.0", inclusive = true, message = "現在価格は0以上である必要があります")
    private BigDecimal currentPrice;

    @NotNull(message = "リードタイムは必須です")
    @Min(value = 0, message = "リードタイムは0以上である必要があります")
    private Integer leadTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @NotNull(message = "ステータスは必須です")
    private SupplierProductStatus status = SupplierProductStatus.ACTIVE;

    @OneToMany(mappedBy = "supplierProduct")
    @Builder.Default
    private List<SupplierProductPriceHistory> priceHistories = new ArrayList<>();
}

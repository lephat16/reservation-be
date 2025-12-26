package com.example.ReservationApp.entity.product;

import com.example.ReservationApp.enums.ProductStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品エンティティ
 * 商品の基本情報（名前、SKU、価格、在庫数量など）を管理。
 * 各商品は1つのカテゴリに属し（ManyToOne）。
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
@Data
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "商品名は必須です")
    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @NotBlank(message = "商品コードは必須です")
    @Size(max = 50, message = "商品コードは50文字以内で入力してください")
    private String productCode;
    private String description;
    private String unit;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = ProductStatus.ACTIVE;
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}

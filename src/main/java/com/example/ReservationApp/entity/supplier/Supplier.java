package com.example.ReservationApp.entity.supplier;

import java.util.ArrayList;
import java.util.List;

import com.example.ReservationApp.enums.SupplierStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仕入先エンティティ
 *
 * 仕入先の基本情報（名前、連絡先、 住所）を管理。
 */
@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "名前は必須です。")
    private String name;
    @NotBlank(message = "連絡先情報は必須です。")
    private String contactInfo;
    @NotBlank(message = "住所は必須です。")
    private String address;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SupplierStatus status  = SupplierStatus.ACTIVE; // ACTIVE / INACTIVE

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SupplierProduct> supplierProducts = new ArrayList<>();
}

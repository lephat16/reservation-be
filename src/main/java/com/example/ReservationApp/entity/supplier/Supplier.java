package com.example.ReservationApp.entity.supplier;

import java.util.ArrayList;
import java.util.List;

import com.example.ReservationApp.enums.SupplierStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "名前は必須です")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "連絡先情報は必須です")
    @Column(nullable = false)
    @Pattern(regexp = "^(0\\d{1,4}-?\\d{1,4}-?\\d{4})$", message = "電話番号の形式が正しくありません")
    private String contactInfo;

    @NotBlank(message = "メールは必須です")
    @Email(message = "メール形式が正しくありません")
    @Column(nullable = false, unique = true)
    private String mail;

    @NotBlank(message = "住所は必須です")
    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SupplierStatus status = SupplierStatus.ACTIVE; // ACTIVE / INACTIVE

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SupplierProduct> supplierProducts = new ArrayList<>();
}

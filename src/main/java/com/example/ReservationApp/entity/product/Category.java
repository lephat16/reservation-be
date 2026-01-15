package com.example.ReservationApp.entity.product;

import java.time.LocalDateTime;

import com.example.ReservationApp.enums.CategoryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品カテゴリを表すエンティティ
 *
 * 各カテゴリは複数のProductを持つこと、
 * カテゴリ削除時には関連するProductも一緒に削除（Cascade.ALL）。
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categories")
@Data
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "名前は必須です")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryStatus status;
    @Column(length = 500, nullable = true)
    private String description;
    @Column(nullable = false)
    private String imageUrl;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) {
            this.status = CategoryStatus.INACTIVE;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

package com.example.ReservationApp.entity.user;

import java.time.LocalDateTime;

import com.example.ReservationApp.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ユーザーエンティティ
 *
 * アプリケーションに登録されたユーザー情報を管理。
 * 名前、メールアドレス、パスワード、電話番号、役割、関連するトランザクションなどを保持。
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Data
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", unique = true)
    private String userId;
    @NotBlank(message = "名前は必須です")
    private String name;
    @NotBlank(message = "メールアドレスは必須です")
    private String email;
    @NotBlank(message = "パスワードは必須です")
    private String password;
    @NotBlank(message = "電話番号は必須です")
    @Column(name = "phone_number")
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /**
     * 作成日時（初期化時に現在日時を設定）
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

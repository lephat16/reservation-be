package com.example.ReservationApp.entity.user;

import java.time.LocalDateTime;

import com.example.ReservationApp.enums.RevokedReason;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String refreshToken;

    private String ipAddress;
    private String userAgent;

    private LocalDateTime expiry;

    private LocalDateTime createdAt;
    private LocalDateTime revokedAt;

    @Enumerated(EnumType.STRING)
    private RevokedReason revokedReason;

    @Builder.Default
    private boolean revoked = false;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

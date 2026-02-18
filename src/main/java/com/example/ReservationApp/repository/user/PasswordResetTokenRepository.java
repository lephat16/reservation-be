package com.example.ReservationApp.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ReservationApp.entity.user.PasswordResetToken;
import com.example.ReservationApp.entity.user.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}

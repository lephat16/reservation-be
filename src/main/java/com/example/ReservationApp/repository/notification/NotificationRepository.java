package com.example.ReservationApp.repository.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ReservationApp.entity.notification.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadedFalse(Long userId);
}
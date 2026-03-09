package com.example.ReservationApp.repository.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.ReservationApp.entity.notification.Notification;

import jakarta.transaction.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadedFalse(Long userId);

    @Modifying
    @Transactional
    @Query("""
                UPDATE Notification n
                SET n.readed = true
                WHERE n.userId = :userId
                AND n.readed = false
            """)
    int markAllAsRead(Long userId);
}
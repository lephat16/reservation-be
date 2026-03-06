package com.example.ReservationApp.service.notification;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.notification.NotificationDTO;

public interface NotificationService {
    ResponseDTO<List<NotificationDTO>> getNotificationsForUser(Long userId);

    ResponseDTO<Long> getUnreadCount(Long userId);

    ResponseDTO<NotificationDTO> markAsRead(Long id);

    ResponseDTO<NotificationDTO> createNotification(NotificationDTO n);
}

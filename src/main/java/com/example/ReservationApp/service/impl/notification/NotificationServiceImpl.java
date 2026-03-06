package com.example.ReservationApp.service.impl.notification;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.notification.NotificationDTO;
import com.example.ReservationApp.entity.notification.Notification;
import com.example.ReservationApp.mapper.NotificationMapper;
import com.example.ReservationApp.repository.notification.NotificationRepository;
import com.example.ReservationApp.service.notification.NotificationService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public ResponseDTO<List<NotificationDTO>> getNotificationsForUser(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<NotificationDTO> notificationDTOs = notificationMapper.toDTOList(notifications);
        return ResponseDTO.<List<NotificationDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("通知一覧の取得に成功しました")
                .data(notificationDTOs)
                .build();
    }

    @Override
    public ResponseDTO<Long> getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndReadedFalse(userId);
        return ResponseDTO.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("未読通知の件数を取得しました")
                .data(count)
                .build();
    }

    @Override
    public ResponseDTO<NotificationDTO> markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知が見つかりません"));
        notification.setReaded(true);
        Notification updated = notificationRepository.save(notification);
        NotificationDTO notificationDTO = notificationMapper.toDTO(updated);
        return ResponseDTO.<NotificationDTO>builder()
                .status(HttpStatus.OK.value())
                .message("通知を既読にしました")
                .data(notificationDTO)
                .build();
    }

    @Override
    public ResponseDTO<NotificationDTO> createNotification(NotificationDTO notification) {
        Notification entity = notificationMapper.toEntity(notification);
        notificationRepository.save(entity);
        return ResponseDTO.<NotificationDTO>builder()
                .status(HttpStatus.OK.value())
                .message("新しい通知を作成しました")
                .data(notification)
                .build();

    }
}

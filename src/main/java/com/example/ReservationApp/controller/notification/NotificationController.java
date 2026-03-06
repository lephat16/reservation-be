package com.example.ReservationApp.controller.notification;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.notification.NotificationDTO;
import com.example.ReservationApp.service.notification.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<NotificationDTO>>> getNotificationsForUser(
            @RequestParam Long userId) {

        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }
     @GetMapping("/unread-count")
    public ResponseEntity<ResponseDTO<Long>> getUnreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }
    @PostMapping("/mark-read")
    public ResponseEntity<ResponseDTO<NotificationDTO>> markAsRead(@RequestParam Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<NotificationDTO>> createNotification(
            @RequestBody NotificationDTO notificationDTO) {

        return ResponseEntity.ok(notificationService.createNotification(notificationDTO));
    }
}

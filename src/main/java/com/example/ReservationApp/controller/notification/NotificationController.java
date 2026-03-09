package com.example.ReservationApp.controller.notification;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.notification.NotificationDTO;
import com.example.ReservationApp.service.notification.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/all-notifications")
    public ResponseEntity<ResponseDTO<List<NotificationDTO>>> getNotificationsForUser(
            @RequestParam Long userId) {

        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    @GetMapping("/notification/unread-count")
    public ResponseEntity<ResponseDTO<Long>> getUnreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PostMapping("/notification/mark-read")
    public ResponseEntity<ResponseDTO<NotificationDTO>> markAsRead(@RequestParam Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @DeleteMapping("/{id}/delete-notification")
    public ResponseEntity<ResponseDTO<Void>> deleteNotification(@PathVariable Long id) {

        return ResponseEntity.ok(notificationService.deleteNotification(id));
    }

    @PostMapping("/notification/mark-read-all")
    public ResponseEntity<ResponseDTO<Void>> markReadAllNotification(@RequestParam Long userId) {

        return ResponseEntity.ok(notificationService.markReadAllNotification(userId));
    }
}

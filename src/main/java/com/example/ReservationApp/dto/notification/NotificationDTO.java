package com.example.ReservationApp.dto.notification;

import java.time.LocalDateTime;

import com.example.ReservationApp.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class NotificationDTO {
    private Long id;
    private String title;
    private Long userId;
    private String message;
    private NotificationType type;
    private String link;
    private boolean readed;
    private LocalDateTime createdAt;
}

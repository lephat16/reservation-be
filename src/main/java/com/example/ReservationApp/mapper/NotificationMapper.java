package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.example.ReservationApp.dto.notification.NotificationDTO;
import com.example.ReservationApp.entity.notification.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationDTO toDTO(Notification notification);

    List<NotificationDTO> toDTOList(List<Notification> notifications);

    Notification toEntity(NotificationDTO dto);
}

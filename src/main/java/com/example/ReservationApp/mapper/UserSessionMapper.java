package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.user.UserSessionDTO;
import com.example.ReservationApp.entity.user.UserSession;

@Mapper(componentModel = "spring")
public interface UserSessionMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "isCurrentSession", ignore = true)
    @Mapping(target = "device", ignore = true)
    @Mapping(target = "status", ignore = true)
    UserSessionDTO toDTO(UserSession userSession);

    List<UserSessionDTO> toDTOList(List<UserSession> userSessions);

    @Mapping(target = "user", ignore = true)
    UserSession toEntity(UserSessionDTO userSessionDTO);
}

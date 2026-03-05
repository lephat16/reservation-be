package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.user.LoginHistoryDTO;
import com.example.ReservationApp.entity.user.LoginHistory;

@Mapper(componentModel = "spring")
public interface LoginHistoryMapper {

    @Mapping(target = "device", ignore = true)
    LoginHistoryDTO toDTO(LoginHistory loginHistory);

    List<LoginHistoryDTO> toDTOList(List<LoginHistory> loginHistorys);
}

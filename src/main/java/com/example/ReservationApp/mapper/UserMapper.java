package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.example.ReservationApp.dto.user.UserDTO;
import com.example.ReservationApp.entity.user.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserDTO toDTO(User user);
    List<UserDTO> toDTOList(List<User> users);
    User toEntity(UserDTO userDTO);
}

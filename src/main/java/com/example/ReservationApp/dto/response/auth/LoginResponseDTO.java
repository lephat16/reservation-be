package com.example.ReservationApp.dto.response.auth;

import com.example.ReservationApp.dto.user.UserDTO;
import com.example.ReservationApp.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {
    private String expirationTime;
    private UserRole role;
    private UserDTO user;
}

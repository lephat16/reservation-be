package com.example.ReservationApp.service.auth;

import java.util.List;
import java.util.Map;

import com.example.ReservationApp.dto.LoginRequestDTO;
import com.example.ReservationApp.dto.RegisterRequestDTO;
import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.auth.LoginResponseDTO;
import com.example.ReservationApp.dto.response.auth.RefreshTokenDTO;
import com.example.ReservationApp.dto.user.UserDTO;

public interface UserService  {
    
    ResponseDTO<UserDTO> registerUser(RegisterRequestDTO registerRequestDTO);

    ResponseDTO<LoginResponseDTO> loginUser(LoginRequestDTO loginRequestDTO);

    ResponseDTO<List<UserDTO>> getAllUsers();

    ResponseDTO<UserDTO> getUserById(Long id);

    ResponseDTO<UserDTO> updateUser(Long id, UserDTO userDTO);
    
    ResponseDTO<Void> deleteUser(Long id);
    
    ResponseDTO<UserDTO> getCurrentLoggedInUser();

    ResponseDTO<RefreshTokenDTO> refresh(Map<String, String> body);
}

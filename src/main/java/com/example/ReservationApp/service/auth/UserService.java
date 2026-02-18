package com.example.ReservationApp.service.auth;

import java.util.List;

import com.example.ReservationApp.dto.LoginRequestDTO;
import com.example.ReservationApp.dto.RegisterRequestDTO;
import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.request.ChangePasswordRequest;
import com.example.ReservationApp.dto.response.auth.LoginResponseDTO;
import com.example.ReservationApp.dto.user.UserDTO;
import com.example.ReservationApp.entity.user.LoginHistory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    ResponseDTO<UserDTO> registerUser(RegisterRequestDTO registerRequestDTO);

    ResponseDTO<LoginResponseDTO> loginUser(LoginRequestDTO loginRequestDTO, HttpServletRequest request,
            HttpServletResponse response);

    ResponseDTO<List<UserDTO>> getAllUsers();

    ResponseDTO<UserDTO> getUserById(Long id);

    ResponseDTO<UserDTO> updateUser(Long id, UserDTO userDTO);

    ResponseDTO<Void> deleteUser(Long id);

    ResponseDTO<UserDTO> getCurrentLoggedInUser();

    ResponseDTO<Void> refresh(HttpServletResponse response, String refreshToken);

    ResponseDTO<Void> logout(HttpServletResponse response, String refreshToken);

    ResponseDTO<UserDTO> changePassword(Long userId, ChangePasswordRequest request);

    ResponseDTO<List<LoginHistory>> getLoginHistory();

    // spring.mail.passwordを用意し次第、また進もう
    // ResponseDTO<Void> sendResetPasswordEmail(String email);

    // ResponseDTO<Void> resetPassword(String token, String newPassword);

}

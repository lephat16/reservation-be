package com.example.ReservationApp.controller.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ReservationApp.dto.LoginRequestDTO;
import com.example.ReservationApp.dto.RegisterRequestDTO;
import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.auth.LoginResponseDTO;
import com.example.ReservationApp.dto.response.auth.RefreshTokenDTO;
import com.example.ReservationApp.dto.user.UserDTO;
import com.example.ReservationApp.service.auth.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 認証に関するAPIを提供するコントローラー
 * 
 * ユーザーの登録およびログイン処理を担当。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 新しいユーザーを登録するエンドポイント
     *
     * @param registerRequestDTO 登録情報を格納したDTO
     * @return 登録結果を含むResponseDTO
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<UserDTO>> registerUser(@RequestBody @Valid RegisterRequestDTO registerRequestDTO) {

        return ResponseEntity.ok(userService.registerUser(registerRequestDTO));
    }

    /**
     * ユーザーをログインさせるエンドポイント
     *
     * @param loginRequestDTO ログイン情報を格納したDTO
     * @return ログイン結果（JWTトークン等）を含むResponseDTO
     */
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> loginUser(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {

        return ResponseEntity.ok(userService.loginUser(loginRequestDTO));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<RefreshTokenDTO>> refresh(@RequestBody Map<String, String> body) {
        
        return ResponseEntity.ok(userService.refresh(body));
    }
}

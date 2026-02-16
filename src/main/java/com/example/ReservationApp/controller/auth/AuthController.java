package com.example.ReservationApp.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ReservationApp.dto.LoginRequestDTO;
import com.example.ReservationApp.dto.RegisterRequestDTO;
import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.auth.LoginResponseDTO;
import com.example.ReservationApp.dto.user.UserDTO;
import com.example.ReservationApp.enums.UserRole;
import com.example.ReservationApp.security.JwtUtils;
import com.example.ReservationApp.service.auth.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final JwtUtils jwtUtils;

    /**
     * 新しいユーザーを登録するエンドポイント
     *
     * @param registerRequestDTO 登録情報を格納したDTO
     * @return 登録結果を含むResponseDTO
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<UserDTO>> registerUser(
            @RequestBody @Valid RegisterRequestDTO registerRequestDTO) {

        return ResponseEntity.ok(userService.registerUser(registerRequestDTO));
    }

    /**
     * ユーザーをログインさせるエンドポイント
     *
     * @param loginRequestDTO ログイン情報を格納したDTO
     * @return ログイン結果（JWTトークン等）を含むResponseDTO
     */
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> loginUser(
            @RequestBody @Valid LoginRequestDTO loginRequestDTO,
            HttpServletResponse response) {

        ResponseDTO<LoginResponseDTO> result = userService.loginUser(loginRequestDTO);

        String accessToken = jwtUtils.generateToken(loginRequestDTO.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(loginRequestDTO.getEmail());

        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15);
        accessCookie.setAttribute("SameSite", "Strict");
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        refreshCookie.setAttribute("SameSite", "Strict");
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(result);
    }

    /**
     * リフレッシュトークンを使用してアクセストークンを再発行するエンドポイント
     *
     * フロント側はトークンを直接扱わず、自動的に Cookie 経由で更新される想定。
     *
     * @param refreshToken Cookie に保存されたリフレッシュトークン
     * @return トークン再発行結果
     */

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<Void>> refresh(
            HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        return ResponseEntity.ok(userService.refresh(response, refreshToken));
    }

    /**
     * ログアウト処理を行うエンドポイント
     *
     * @return ログアウト結果
     */
    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO<Void>> logout(HttpServletResponse response) {

        return ResponseEntity.ok(userService.logout(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<UserDTO>> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(ResponseDTO.<UserDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("Not logged in")
                    .data(null)
                    .build());
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserDTO userDTO = UserDTO.builder()
                .email(userDetails.getUsername())
                .role(UserRole.valueOf(
                        userDetails.getAuthorities().iterator().next().getAuthority())) // lấy role đầu tiên
                .build();

        return ResponseEntity.ok(ResponseDTO.<UserDTO>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(userDTO)
                .build());
    }

}

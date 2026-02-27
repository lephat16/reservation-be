package com.example.ReservationApp.controller.auth;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.request.ChangePasswordRequest;
import com.example.ReservationApp.dto.user.CreatePasswordDTO;
import com.example.ReservationApp.dto.user.CreateUserDTO;
import com.example.ReservationApp.dto.user.ResendPasswordDTO;
import com.example.ReservationApp.dto.user.UserDTO;
import com.example.ReservationApp.dto.user.UserSessionDTO;
import com.example.ReservationApp.entity.user.LoginHistory;
import com.example.ReservationApp.entity.user.TokenType;
import com.example.ReservationApp.security.AuthUser;
import com.example.ReservationApp.service.auth.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * ユーザー管理用のAPIコントローラー
 *
 * ユーザーの取得、更新、削除を担当。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * すべてのユーザーを取得するエンドポイント
     *
     * @return 全ユーザー情報
     */
    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * 指定IDのユーザーを取得するエンドポイント
     *
     * @param id 取得したいユーザーのID
     * @return 該当ユーザー情報
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * 指定IDのユーザー情報を更新するエンドポイント
     *
     * @param id      更新対象ユーザーのID
     * @param userDTO 更新内容を含むUserDTO
     * @return 更新後のユーザー情報
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    /**
     * 指定IDのユーザーを削除するエンドポイント
     *
     * @param id 削除対象ユーザーのID
     * @return 削除後のユーザー情報
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    /**
     * 現在ログイン中のユーザー情報を取得するエンドポイント
     *
     * @return ログイン中ユーザーの情報
     */
    @GetMapping("/current")
    public ResponseEntity<ResponseDTO<UserDTO>> getLogginedUser() {

        return ResponseEntity.ok(userService.getCurrentLoggedInUser());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<UserDTO>> createUserByAdmin(
            @Valid @RequestBody CreateUserDTO registerRequestDTO,
            @AuthenticationPrincipal AuthUser authUser) {

        return ResponseEntity.ok(userService.createUserByAdmin(registerRequestDTO, authUser));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ResponseDTO<UserDTO>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {

        return ResponseEntity.ok(userService.changePassword(id, request));
    }

    @GetMapping("/login-history")
    public ResponseEntity<ResponseDTO<List<LoginHistory>>> getLoginHistory() {

        return ResponseEntity.ok(userService.getLoginHistory());
    }

    @PostMapping("/send-reset-password")
    public ResponseEntity<ResponseDTO<Void>> sendPasswordTokenEmail(@RequestBody ResendPasswordDTO request) {

        return ResponseEntity.ok(userService.sendPasswordTokenEmail(request.getEmail(), TokenType.RESET_PASSWORD, 30));
    }

    @PostMapping("/set-password")
    public ResponseEntity<ResponseDTO<Void>> setPassword(
            @Valid @RequestBody CreatePasswordDTO request) {
        return ResponseEntity.ok(userService.createPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseDTO<Void>> resetPassword(
            @Valid @RequestBody CreatePasswordDTO dto) {

        return ResponseEntity.ok(userService.resetPassword(dto.getToken(),
                dto.getPassword()));
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<ResponseDTO<Void>> verifySetPasswordToken(@RequestParam String token) {

        return ResponseEntity.ok(userService.verifySetPasswordToken(token));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/sessions")
    public ResponseEntity<ResponseDTO<List<UserSessionDTO>>> getUserSessions(
            @PathVariable Long id,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        return ResponseEntity.ok(userService.getUserSessions(id, accessToken));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sessions/{sessionId}/revoke")
    public ResponseEntity<ResponseDTO<Void>> revokeSession(@PathVariable Long sessionId) {

        return ResponseEntity.ok(userService.revokeSession(sessionId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}/sessions/revoke-all")
    public ResponseEntity<ResponseDTO<Void>> revokeAllSessions(@PathVariable Long userId) {

        return ResponseEntity.ok(userService.revokeAllSessions(userId));
    }

}

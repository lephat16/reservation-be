package com.example.ReservationApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ログインリクエスト用のDTO
 *
 * ユーザーのログイン情報を受け取るために使用。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {
    @NotBlank(message = "メールアドレスは必須です")
    private String email;
    @NotBlank(message = "パスワードは必須です")
    private String password;

    private boolean isRemember = false;
}

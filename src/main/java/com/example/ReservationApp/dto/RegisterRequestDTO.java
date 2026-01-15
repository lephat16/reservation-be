package com.example.ReservationApp.dto;

import com.example.ReservationApp.enums.UserRole;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登録クエスト用のDTO
 *
 * ユーザーの登録情報を受け取るために使用。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    
    @NotBlank(message = "名前は必須です")
    private String name;
    @NotBlank(message = "メールアドレスは必須です")
    private String email;
    @NotBlank(message = "パスワードは必須です")
    private String password;
    @NotBlank(message = "電話番号は必須です")
    private String phoneNumber;

    private UserRole role;
}

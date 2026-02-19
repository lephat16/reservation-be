package com.example.ReservationApp.dto.user;

import com.example.ReservationApp.enums.UserRole;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
    @NotBlank(message = "名前は必須です")
    private String name;
    @NotBlank(message = "メールアドレスは必須です")
    private String email;

    @NotBlank(message = "電話番号は必須です")
    private String phoneNumber;

    private UserRole role;
}

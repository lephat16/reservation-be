package com.example.ReservationApp.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePasswordDTO {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, message = "パスワードは8文字以上必要です")
    private String password;
}


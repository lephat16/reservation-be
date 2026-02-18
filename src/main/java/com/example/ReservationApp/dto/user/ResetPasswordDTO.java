package com.example.ReservationApp.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class ResetPasswordDTO {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8)
    private String newPassword;
}

package com.example.ReservationApp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Datagit
public class ForgotPasswordDTO {
    @NotBlank
    @Email
    private String email;
}

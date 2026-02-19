package com.example.ReservationApp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class ResendPasswordDTO {
    @NotBlank
    @Email
    private String email;
}

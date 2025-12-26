package com.example.ReservationApp.dto.response.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenDTO {
    
    private String token;
    private String refreshToken;
}

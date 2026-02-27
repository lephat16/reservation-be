package com.example.ReservationApp.dto.user;

import java.time.LocalDateTime;

import com.example.ReservationApp.enums.RevokedReason;
import com.example.ReservationApp.enums.SessionStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSessionDTO {

    private Long id;
    private String userId;

    private String refreshToken;

    private String ipAddress;
    private String userAgent;

    private String device;
    private SessionStatus status;

    private LocalDateTime expiry;

    private LocalDateTime createdAt;
    private LocalDateTime revokedAt;

    private RevokedReason revokedReason;
    private boolean revoked;
    private boolean isCurrentSession;

}

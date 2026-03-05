package com.example.ReservationApp.dto.user;

import java.time.LocalDateTime;

import com.example.ReservationApp.enums.LoginStatus;
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
public class LoginHistoryDTO {
    private Long id;
    private String userId;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String device;

    private LoginStatus status;
}

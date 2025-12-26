package com.example.ReservationApp.dto.user;

import java.time.LocalDateTime;

import com.example.ReservationApp.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ユーザー用DTO
 * データベースのUserエンティティと対応、
 * クライアントとサーバー間でユーザー情報を安全にやり取りするために使用。
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {

    private Long id;
    private String userId;
    private String name;
    private String email;

    @JsonIgnore
    private String password;
    private String phoneNumber;
    private UserRole role;
    private LocalDateTime createdAt;
}

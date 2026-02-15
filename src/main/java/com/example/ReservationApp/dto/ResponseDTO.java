package com.example.ReservationApp.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

/**
 * APIレスポンス用DTO
 *
 * サーバーからクライアントへのレスポンス情報を格納するためのDTOです。
 * 各リスポンスに必要な情報（状態コード、メッセージ、ユーザー情報、JWTトークンなど）を保持。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {

    // Status info
    private int status;
    private String message;
    private final LocalDateTime timestamp = LocalDateTime.now();

    // generic data
    private T data;
    private Long total;

    private List<String> errors;
}

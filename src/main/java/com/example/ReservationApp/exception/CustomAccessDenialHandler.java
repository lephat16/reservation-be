package com.example.ReservationApp.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.ReservationApp.dto.ResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * カスタムアクセス拒否ハンドラー
 *
 * Spring Securityでアクセスが拒否された場合に呼び出。
 * HTTPステータス403（Forbidden）を返し、JSON形式でエラーメッセージをクライアントに返却。
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDenialHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * アクセス拒否時に呼び出されるハンドラ
     *
     * @param request HTTPリクエスト
     * @param response HTTPレスポンス
     * @param accessException アクセス拒否例外
     * @throws ServletException サーブレット例外
     * @throws IOException 入出力例外
     */
    @Override
    public void handle(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessException) throws ServletException, IOException {
        ResponseDTO<Void> errResponseDTO = ResponseDTO.<Void>builder()
                                            .status(HttpStatus.FORBIDDEN.value())
                                            .message(accessException.getMessage())
                                            .build();
        
        response.setContentType("application/json");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getWriter().write(objectMapper.writeValueAsString(errResponseDTO));
    }
}

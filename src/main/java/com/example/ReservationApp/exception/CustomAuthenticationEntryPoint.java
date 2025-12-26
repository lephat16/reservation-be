package com.example.ReservationApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.ReservationApp.dto.ResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

/**
 * カスタム認証エントリポイント
 *
 * 認証が必要なリソースに未認証の状態でアクセスした場合に呼び出。
 * HTTPステータス401（Unauthorized）を返し、JSON形式でエラーメッセージをクライアントに返却。
 */
@Component
@AllArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint{
    
    private final ObjectMapper objectMapper;
    
    /**
     * 認証失敗時に呼び出されるハンドラ
     *
     * @param request HTTPリクエスト
     * @param response HTTPレスポンス
     * @param authException 認証例外
     * @throws ServletException サーブレット例外
     * @throws JsonProcessingException JSON処理例外
     * @throws java.io.IOException 入出力例外
     */
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException)
            throws ServletException, JsonProcessingException, java.io.IOException {
                ResponseDTO<Void> errorResponseDTO = ResponseDTO.<Void>builder()
                                                        .status(HttpStatus.UNAUTHORIZED.value())
                                                        .message(authException.getMessage())
                                                        .build();
                response.setContentType("application/json");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write(objectMapper.writeValueAsString(errorResponseDTO));
            }
}

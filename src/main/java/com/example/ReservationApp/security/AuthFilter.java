package com.example.ReservationApp.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 各HTTPリクエストに対してJWTトークンを検証、
 * 認証情報をSecurityContextに設定するためのフィルタークラス。
 * このクラスはOncePerRequestFilterを継承、
 * リクエストごとに1回だけ処理。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * HTTPリクエストごとに呼ばれるメイン処理。
     * AuthorizationヘッダーからJWTを取得し、検証後にSecurityContextに認証情報を設定。
     * 
     * @param request     HTTPリクエスト
     * @param response    HTTPレスポンス
     * @param filterChain フィルター連鎖
     * @throws ServletException サーブレット例外
     * @throws IOException      入出力例外
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = getTokenFromCookies(request);
        if (token != null) {
            try {
                String email = jwtUtils.extractUsername(token);

                if (StringUtils.hasText(email) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                    if (jwtUtils.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
            } catch (ExpiredJwtException e) {
                log.warn("JWT expired: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (Exception e) {
                log.error("Invalid JWT: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

        }
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("AuthFilterで例外が発生しました: {}", e.getMessage(), e);
            throw e;
        }

    }

    /**
     * AuthorizationヘッダーからJWTを取得。
     * ヘッダーが「Bearer 」で始まる場合のみトークンを返。
     * 
     * @param request HTTPリクエスト
     * @return JWTトークン、存在しない場合はnull
     */
    private String getTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (("accessToken".equals(cookie.getName()))) {
                    return cookie.getValue();
                }
            }
        }
        return null;
        
    }
}

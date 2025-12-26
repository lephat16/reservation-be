package com.example.ReservationApp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.ReservationApp.exception.CustomAccessDenialHandler;
import com.example.ReservationApp.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;

/**
 * SecurityConfigクラスは、Spring Securityの設定を定義。
 * JWT認証、認可、例外ハンドリング、CORS、CSRFなどのセキュリティ機能を設定。
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAccessDenialHandler customAccessDenialHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    
    private final AuthFilter authFilter;

    /**
     * セキュリティフィルターチェーンを構築。
     * - CSRF保護を無効化（JWTを使用する場合）
     * - CORS設定をデフォルトで有効化
     * - 認証・認可例外ハンドリング
     * - エンドポイントごとのアクセス制御
     * - JWTフィルターをUsernamePasswordAuthenticationFilterの前に追加
     * 
     * @param httpSecurity HttpSecurityオブジェクト
     * @return SecurityFilterChain
     * @throws Exception フィルター設定時の例外
     */
    @Bean 
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable())
                    .cors(Customizer.withDefaults())
                    .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDenialHandler)
                    )
                    .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                    )
                    .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    /**
     * パスワードエンコーダーBeanを定義。
     * 
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManagerを取得するBeanを定義。
     * 
     * @param authenticationConfiguration AuthenticationConfigurationオブジェクト
     * @return AuthenticationManager
     * @throws Exception 取得時の例外
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }
}

package com.example.ReservationApp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
         * セキュリティフィルターチェーンを構築。Swagger のルートに関するセキュリティ設定。
         * - Swaggerにアクセスできるように公開設定。
         * - Swaggerのルートに対するリクエストを許可し、それ以外のリクエストはすべて拒否。
         * 
         * @param httpSecurity HttpSecurityオブジェクト
         * @return SecurityFilterChain
         * @throws Exception セキュリティ設定時の例外
         */
        @Bean
        @Order(1)
        public SecurityFilterChain swaggerSecurity(HttpSecurity httpSecurity) throws Exception {
                httpSecurity.csrf(csrf -> csrf.disable()) // CSRF保護を無効化（Swaggerの場合は不要）
                                .cors(Customizer.withDefaults()) // CORS設定をデフォルトで有効化
                                .authorizeHttpRequests(request -> request
                                                .requestMatchers(
                                                                "/swagger-ui/**", // Swagger UI
                                                                "/v3/api-docs/**", // Swagger Docs
                                                                "/v3/api-docs.yaml", // Swagger YAML
                                                                "/swagger-ui/index.html", // Swaggerのインデックスページ
                                                                "/swagger-ui/oauth2-redirect.html", // SwaggerのOAuth2リダイレクト
                                                                "/webjars/**" // Swagger WebJars
                                                ).permitAll() // これらのエンドポイントへのアクセスは全て許可
                                                .anyRequest().denyAll()) // その他のリクエストは全て拒否

                                // Swagger用のセキュリティマッチャーを適用、Swaggerルートに対してのみ適用される
                                .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui/index.html",
                                                "/webjars/**");

                return httpSecurity.build();
        }

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
        @Order(2)
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
                httpSecurity.csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                                                .accessDeniedHandler(customAccessDenialHandler))
                                .authorizeHttpRequests(request -> request
                                                .requestMatchers("/api/auth/**",
                                                                "/api/users/set-password",
                                                                "/api/users/reset-password",
                                                                "/api/users/verify-reset-token",
                                                                "/api/users/send-reset-password")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                                .securityMatcher("/api/**");
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
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }
}
